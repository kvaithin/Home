package com.softserveinc.ita.homeproject.homeservice.service.cooperation.apartment;

import java.time.LocalDateTime;
import java.util.List;

import com.softserveinc.ita.homeproject.homedata.cooperation.apatment.Apartment;
import com.softserveinc.ita.homeproject.homedata.cooperation.apatment.ApartmentRepository;
import com.softserveinc.ita.homeproject.homedata.cooperation.house.House;
import com.softserveinc.ita.homeproject.homedata.cooperation.house.HouseRepository;
import com.softserveinc.ita.homeproject.homedata.cooperation.invitation.apartment.ApartmentInvitation;
import com.softserveinc.ita.homeproject.homeservice.dto.cooperation.apartment.ApartmentDto;
import com.softserveinc.ita.homeproject.homeservice.dto.cooperation.invitation.apartment.ApartmentInvitationDto;
import com.softserveinc.ita.homeproject.homeservice.exception.BadRequestHomeException;
import com.softserveinc.ita.homeproject.homeservice.exception.NotFoundHomeException;
import com.softserveinc.ita.homeproject.homeservice.mapper.ServiceMapper;
import com.softserveinc.ita.homeproject.homeservice.service.cooperation.invitation.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApartmentServiceImpl implements ApartmentService {

    private final InvitationService<ApartmentInvitation, ApartmentInvitationDto> invitationService;

    private final ApartmentRepository apartmentRepository;

    private final HouseRepository houseRepository;

    private final ServiceMapper mapper;

    private static final String HOUSE_WITH_ID_NOT_FOUND = "House with 'id: %d' is not found";

    private static final String APARTMENT_WITH_ID_NOT_FOUND = "Apartment with 'id: %d' is not found";

    public ApartmentServiceImpl(
        @Qualifier("apartmentInvitationServiceImpl") InvitationService<ApartmentInvitation, ApartmentInvitationDto>
            invitationService,
        ApartmentRepository apartmentRepository,
        HouseRepository houseRepository,
        ServiceMapper mapper) {
        this.invitationService = invitationService;
        this.apartmentRepository = apartmentRepository;
        this.houseRepository = houseRepository;
        this.mapper = mapper;
    }


    @Transactional
    @Override
    public ApartmentDto createApartment(Long houseId, ApartmentDto createApartmentDto) {

        if (apartmentRepository.findByApartmentNumberAndHouseId(createApartmentDto.getApartmentNumber(), houseId)
            .isPresent()) {
            throw new BadRequestHomeException("Apartment with number " + createApartmentDto.getApartmentNumber()
                + " already exist in this house");
        }

        House house = houseRepository.findById(houseId).filter(House::getEnabled)
            .orElseThrow(() -> new NotFoundHomeException(String.format(HOUSE_WITH_ID_NOT_FOUND, houseId)));

        Apartment apartment = mapper.convert(createApartmentDto, Apartment.class);
        List<ApartmentInvitationDto> invitations = createApartmentDto.getInvitations();

        apartment.setHouse(house);
        apartment.setInvitations(null);
        apartment.setCreateDate(LocalDateTime.now());
        apartment.setEnabled(true);

        apartmentRepository.save(apartment);

        invitations.forEach(invitation -> {
            invitation.setApartmentNumber(apartment.getApartmentNumber());
            invitation.setApartmentId(apartment.getId());
        });
        invitations.forEach(invitationService::createInvitation);

        return mapper.convert(apartment, ApartmentDto.class);
    }

    @Transactional
    @Override
    public ApartmentDto updateApartment(Long houseId, Long apartmentId, ApartmentDto updateApartmentDto) {
        Apartment toUpdate = apartmentRepository.findById(apartmentId)
            .filter(Apartment::getEnabled)
            .filter(apartment -> apartment.getHouse().getId().equals(houseId))
            .orElseThrow(() ->
                new NotFoundHomeException(
                    String.format(APARTMENT_WITH_ID_NOT_FOUND, apartmentId)));

        if (updateApartmentDto.getApartmentNumber() != null) {
            toUpdate.setApartmentNumber(updateApartmentDto.getApartmentNumber());
        }
        if (updateApartmentDto.getApartmentArea() != null) {
            toUpdate.setApartmentArea(updateApartmentDto.getApartmentArea());
        }

        toUpdate.setUpdateDate(LocalDateTime.now());
        apartmentRepository.save(toUpdate);
        return mapper.convert(toUpdate, ApartmentDto.class);
    }

    @Override
    public void deactivateApartment(Long houseId, Long apartmentId) {
        Apartment toDelete = apartmentRepository.findById(apartmentId)
            .filter(Apartment::getEnabled)
            .filter(apartment -> apartment.getHouse().getId().equals(houseId))
            .orElseThrow(() ->
                new NotFoundHomeException(
                    String.format(APARTMENT_WITH_ID_NOT_FOUND, apartmentId)));

        toDelete.setEnabled(false);
        apartmentRepository.save(toDelete);
    }

    @Override
    @Transactional
    public Page<ApartmentDto> findAll(Integer pageNumber, Integer pageSize, Specification<Apartment> specification) {
        specification = specification
            .and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
                .equal(root.get("house").get("enabled"), true));
        return apartmentRepository.findAll(specification, PageRequest.of(pageNumber - 1, pageSize))
            .map(apartment -> mapper.convert(apartment, ApartmentDto.class));
    }
}
