package cz.metacentrum.perun.spRegistration.service.mails;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDetails {

    private PrivateKey privateKey;
    private X509Certificate x509Certificate;

}
