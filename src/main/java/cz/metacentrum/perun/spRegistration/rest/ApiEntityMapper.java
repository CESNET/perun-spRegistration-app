package cz.metacentrum.perun.spRegistration.rest;

import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.common.models.RequestDTO;
import cz.metacentrum.perun.spRegistration.common.models.RequestSignatureDTO;
import cz.metacentrum.perun.spRegistration.rest.models.RequestOverview;
import cz.metacentrum.perun.spRegistration.rest.models.RequestSignature;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ApiEntityMapper {

    public static RequestOverview mapRequestDTOToRequestOverview(@NonNull RequestDTO dto,
                                                                 @NonNull AttributesProperties attrs)
    {
        RequestOverview r = new RequestOverview();
        r.setId(dto.getReqId());
        ProvidedService s = dto.getProvidedService();
        if (s == null) {
            r.setServiceName(dto.getFacilityName(attrs.getNames().getServiceName()));
            r.setServiceIdentifier("-");
        } else {
            r.setServiceName(dto.getProvidedService().getName());
            r.setServiceIdentifier(dto.getProvidedService().getIdentifier());
        }
        r.setRequesterId(dto.getReqUserId());
        r.setServiceId(dto.getFacilityId());
        r.setAction(dto.getAction());
        r.setStatus(dto.getStatus());
        return r;
    }

    public static List<RequestOverview> mapRequestDTOsToRequestOverviews(List<RequestDTO> dtos,
                                                                         @NonNull AttributesProperties attrs)
    {
        final List<RequestOverview> res = new ArrayList<>();
        if (dtos != null && !dtos.isEmpty()) {
            dtos.forEach(dto -> res.add(mapRequestDTOToRequestOverview(dto, attrs)));
        }
        return res;
    }

    public static RequestSignature mapRequestSignatureDTOsToRequestSignature(RequestSignatureDTO dto) {
        RequestSignature r = new RequestSignature();
        r.setSignerId(dto.getUserId());
        r.setSignerName(dto.getName());
        r.setSignedAt(dto.getSignedAt());
        r.setApproved(dto.isApproved());
        return r;
    }

    public static List<RequestSignature> mapRequestSignatureDTOsToRequestSignature(List<RequestSignatureDTO> dtos) {
        final List<RequestSignature> res = new ArrayList<>();
        if (dtos != null && !dtos.isEmpty()) {
            dtos.forEach(dto -> res.add(mapRequestSignatureDTOsToRequestSignature(dto)));
        }
        return res;
    }

}
