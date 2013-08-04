#!/bin/bash
currentdir=$(dirname $0)
echo $currentdir
mkdir ${currentdir}/sslcerts
cd ${currentdir}/sslcerts

if [ "x${JAVA_HOME}" == "x" ] ; then
    echo "You must set the environment variable JAVA_HOME to the jdk"
    exit 1;
else
    if [ -f $JAVA_HOME/bin/keytool ];
    then
       : # do nothing
    else
        echo  "$JAVA_HOME/bin/keytool not found"
        exit;
    fi
fi

opensslcnf="/System/Library/OpenSSL/openssl.cnf"

if [ ! -f ${opensslcnf} ]; then
    opensslcnf="/opt/local/etc/openssl/openssl.cnf"
    if [ ! -f ${opensslcnf} ]; then
	    opensslcnf="/etc/pki/tls/openssl.cnf"
    fi
fi



echo "====================="
echo "Certing Test Certs fro 365 days (see http://sourceforge.net/projects/portecle/) for a gui for viewing certs"
echo "====================="



echo "--------------------"
echo "Creating the Server "
echo "--------------------"

openssl req -newkey rsa:1024 -nodes -out cacerts.csr -keyout cacerts.key -days 1825 \
 -subj "/C=GB/ST=London/L=London/O=Greencheek/OU=Development/CN=Root CA/emailAddress=root-ca@greencheek.org"

openssl x509 -req -trustout -signkey cacerts.key -days 365 -req -in cacerts.csr \
 -out cacerts.pem -extfile ${opensslcnf} -extensions v3_ca

$JAVA_HOME/bin/keytool -import -keystore cacerts.jks -file cacerts.pem \
 -alias ROOT_CA -storepass cacerts -noprompt


openssl req -newkey rsa:1024 -nodes -out server.csr -keyout server.key \
 -days 120 -subj "/CN=localhost/OU=Development/O=Greencheek/L=London/ST=London/C=UK/emailAddress=server@greencheek.org"

 
openssl x509 -CA cacerts.pem -CAkey cacerts.key -set_serial 02 \
 -req -in server.csr -out server.pem -days 365 \
-extfile ${opensslcnf} -extensions v3_ca

$JAVA_HOME/bin/keytool -import -keystore server.truststore \
 -file cacerts.pem -alias ROOT_CA -storepass changeit -noprompt

$JAVA_HOME/bin/keytool -import -keystore server.truststore \
 -file server.pem -alias tomcat-sv -storepass changeit -noprompt

openssl pkcs12 -export -in server.pem -inkey server.key \
 -certfile cacerts.pem -out server.p12 -passout pass:changeit
 
echo "--------------------"
echo "Creating the Client, Signed by the Server "
echo "--------------------"

openssl req -newkey rsa:1024 -nodes -out client.csr -keyout client.key \
 -days 1825 -subj "/CN=Client/OU=Development/O=Greencheek/L=London/ST=London/C=UK/emailAddress=client@greencheek.org"

openssl x509 -CA server.pem -CAkey server.key \
 -set_serial 02 -req -in client.csr -out client.pem -days 365 \
-extfile ${opensslcnf} -extensions v3_ca

openssl pkcs12 -export -in client.pem -inkey client.key \
 -certfile server.pem -out client.p12 -passout pass:changeit
 
echo "--------------------"
echo "Creating the Client TrustStore containing the Server Public Cert "
echo "--------------------"

$JAVA_HOME/bin/keytool -import -keystore client.truststore \
 -file server.pem -alias tomcat -storepass changeit -noprompt


echo "--------------------"
echo "Creating the Random Cert (should have no access to server)"
echo "--------------------"

openssl req -newkey rsa:1024 -nodes -out random.csr -keyout random.key \
 -days 1825 -subj "/CN=Random/OU=Development/O=Greencheek/L=London/ST=London/C=UK/emailAddress=random@greencheek.org"
 
openssl x509 -req -trustout -signkey random.key -days 365 -req -in random.csr \
 -out random.pem -extfile ${opensslcnf} -extensions v3_ca
 
openssl pkcs12 -export -in random.pem -inkey random.key \
 -out random.p12 -passout pass:random
 
$JAVA_HOME/bin/keytool -import -keystore random.truststore \
 -file random.pem -alias tomcat -storepass random -noprompt 



echo "--------------------"
echo "Creating the Client that is expired"
echo "--------------------"

openssl req -newkey rsa:1024 -nodes -out client-expired.csr -keyout client-expired.key \
 -days 1825 -subj "/CN=Client Expired/OU=Development/O=Greencheek/L=London/ST=London/C=UK/emailAddress=client-expired@greencheek.org"

openssl x509 -CA server.pem -CAkey server.key \
 -set_serial 02 -req -in client-expired.csr -out client-expired.pem -days -1 \
-extfile ${opensslcnf} -extensions v3_ca

$JAVA_HOME/bin/keytool -import -keystore client-expired.truststore \
 -file server.pem -alias tomcat -storepass changeit-expired -noprompt

openssl pkcs12 -export -in client-expired.pem -inkey client-expired.key \
 -certfile server.pem -out client-expired.p12 -passout pass:changeit-expired


echo "--------------------"
echo " Creating the truststore that trusts everything"
echo "--------------------"

$JAVA_HOME/bin/keytool -import -keystore allcerts.truststore \
 -file cacerts.pem -alias ROOT_CA -storepass allcerts -noprompt

$JAVA_HOME/bin/keytool -import -keystore allcerts.truststore \
 -file server.pem -alias tomcat-sv -storepass allcerts -noprompt

$JAVA_HOME/bin/keytool -import -keystore allcerts.truststore \
 -file random.pem -alias random -storepass allcerts -noprompt
 
$JAVA_HOME/bin/keytool -import -keystore allcerts.truststore \
 -file client.pem -alias client -storepass allcerts -noprompt
 
$JAVA_HOME/bin/keytool -import -keystore allcerts.truststore \
 -file client-expired.pem -alias client-expired -storepass allcerts -noprompt
