package cz.metacentrum.perun.spRegistration.service.mails;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.bouncycastle.cms.CMSAlgorithm.AES256_GCM;


@Slf4j
public class CertificateUtil {

    public static void signMessage(MimeMessage message, MailProperties mailProperties) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        CertificateDetails certDetails = getCertificateDetails(mailProperties.getCertificatePath(),
                mailProperties.getPrivateKeyPath(), mailProperties.getPrivateKeyAlgorithm());

        if (certDetails == null || certDetails.getPrivateKey() == null || certDetails.getX509Certificate() == null) {
            return;
        }

        // Create the SMIMESignedGenerator
        SMIMECapabilityVector capabilities = new SMIMECapabilityVector();
        capabilities.addCapability(AES256_GCM);

        ASN1EncodableVector attributes = new ASN1EncodableVector();
        attributes.add(new SMIMECapabilitiesAttribute(capabilities));

        IssuerAndSerialNumber issAndSer = new IssuerAndSerialNumber(
                new X500Name(certDetails.getX509Certificate().getIssuerDN().getName()),
                certDetails.getX509Certificate().getSerialNumber());
        attributes.add(new SMIMEEncryptionKeyPreferenceAttribute(issAndSer));

        SMIMESignedGenerator signer = new SMIMESignedGenerator();

        signer.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder()
                .setSignedAttributeGenerator(new AttributeTable(attributes))
                .build(mailProperties.getSignatureProviderAlgorithm(),
                        certDetails.getPrivateKey(), certDetails.getX509Certificate()));

        List<X509Certificate> certList = new ArrayList<>();
        certList.add(certDetails.getX509Certificate());

        JcaCertStore bcerts = new JcaCertStore(certList);
        signer.addCertificates(bcerts);

        MimeMultipart mm = signer.generate(message);
        message.setContent(mm, mm.getContentType());
        message.saveChanges();
    }

    private static CertificateDetails getCertificateDetails(String certPath, String privKeyPath, String privKeyAlg) {
        try {
            X509Certificate cert = loadCertificate(certPath);
            PrivateKey privateKey = loadPrivateKey(privKeyPath, privKeyAlg);
            if (cert == null || privateKey == null) {
                return null;
            }
            return new CertificateDetails(privateKey, cert);
        } catch (CertificateException e) {
            log.error("Failure when reading signature credentials");
            log.debug("{}", e.getMessage(), e);
        }

        return null;
    }

    private static PrivateKey loadPrivateKey(String path, String algorithm) {
        if (!StringUtils.hasText(path)) {
            return null;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.matches(".*BEGIN.*") && !line.matches(".*END.*")) {
                    content.append(line);
                }
            }
            String privateKeyPEM = content.toString();
            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM.getBytes());

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("Failure when reading private key");
            log.debug("{}", e.getMessage(), e);
        }
        return null;
    }

    private static X509Certificate loadCertificate(String path) throws CertificateException {
        if (!StringUtils.hasText(path)) {
            return null;
        }
        X509Certificate cert = null;
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        try (FileInputStream fis = new FileInputStream(path)) {
            cert = (X509Certificate) factory.generateCertificate(fis);
        } catch (IOException e) {
            log.error("Failure when reading certificate");
            log.debug("{}", e.getMessage(), e);
        }
        return cert;
    }

}
