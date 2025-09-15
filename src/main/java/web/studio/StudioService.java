package web.studio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.exception.AppException;
import web.common.exception.ErrorCode;
import web.common.util.Ulids;
import web.location.AddressRepository;
import web.location.PostalCodeReferenceRepository;
import web.location.entity.AddressEntity;
import web.location.entity.PostalCodeReferenceEntity;
import web.studio.dto.StudioCreateRequest;
import web.studio.dto.StudioResponse;
import web.studio.entity.StudioEntity;
import web.user.UserRepository;
import web.user.entity.UserEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudioService {

    private static final Logger logger = LoggerFactory.getLogger(StudioService.class);

    private final StudioRepository studioRepository;
    private final AddressRepository addressRepository;
    private final PostalCodeReferenceRepository postalCodeRepository;
    private final UserRepository userRepository;

    @Autowired
    public StudioService(
            StudioRepository studioRepository,
            AddressRepository addressRepository,
            PostalCodeReferenceRepository postalCodeRepository,
            UserRepository userRepository) {
        this.studioRepository = studioRepository;
        this.addressRepository = addressRepository;
        this.postalCodeRepository = postalCodeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new studio with address
     */
    @Transactional
    public StudioResponse createStudio(StudioCreateRequest request, String currentUserId) {
        logger.info("Creating new studio: {} by user: {}", request.getName(), currentUserId);

        // Validate business rules
        validateStudioCreationRequest(request);

        // Validate and fetch postal code reference
        PostalCodeReferenceEntity postalCodeRef = validateAndFetchPostalCode(request.getAddress().getPostalCodeId());

        // Validate and fetch owner
        UserEntity owner = validateAndFetchOwner(request.getOwnerId(), currentUserId);

        // Create address first
        AddressEntity address = createAddress(request.getAddress(), postalCodeRef, currentUserId);

        // Create studio
        StudioEntity studio = createStudioEntity(request, address, owner, currentUserId);

        // Save studio
        StudioEntity savedStudio = studioRepository.save(studio);
        logger.info("Successfully created studio: {} with ID: {}", savedStudio.getName(), savedStudio.getId());

        // Convert to response
        return convertToStudioResponse(savedStudio);
    }

    /**
     * Find studio by ID
     */
    @Transactional(readOnly = true)
    public StudioResponse findStudioById(String id) {
        StudioEntity studio = studioRepository.findActiveById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STUDIO_NOT_FOUND, "Studio not found: " + id));
        
        return convertToStudioResponse(studio);
    }

    /**
     * Find studios by owner ID
     */
    @Transactional(readOnly = true)
    public List<StudioResponse> findStudiosByOwnerId(String ownerId) {
        List<StudioEntity> studios = studioRepository.findActiveByOwnerId(ownerId);
        return studios.stream()
                .map(this::convertToStudioResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find studios by owner with pagination
     */
    @Transactional(readOnly = true)
    public Page<StudioResponse> findStudiosByOwner(String ownerId, Pageable pageable) {
        Page<StudioEntity> studioPage = studioRepository.findByOwnerId(ownerId, pageable);
        return studioPage.map(this::convertToStudioResponse);
    }

    /**
     * Search studios by name
     */
    @Transactional(readOnly = true)
    public List<StudioResponse> searchStudiosByName(String searchTerm, Pageable pageable) {
        Page<StudioEntity> studioPage = studioRepository.searchByName(searchTerm, pageable);
        return studioPage.getContent().stream()
                .map(this::convertToStudioResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find studios in geographic area
     */
    @Transactional(readOnly = true)
    public List<StudioResponse> findStudiosByGeographicArea(String countryCode, String provinceCode, Pageable pageable) {
        List<StudioEntity> studios = studioRepository.findByGeographicArea(countryCode, provinceCode, pageable);
        return studios.stream()
                .map(this::convertToStudioResponse)
                .collect(Collectors.toList());
    }

    // Private helper methods

    /**
     * Validate studio creation request
     */
    private void validateStudioCreationRequest(StudioCreateRequest request) {
        // Check email uniqueness
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (studioRepository.existsByEmailAndActive(request.getEmail().trim(), null)) {
                throw new AppException(ErrorCode.STUDIO_EMAIL_ALREADY_EXISTS, 
                    "Email already exists: " + request.getEmail());
            }
        }

        // Check studio code uniqueness
        if (studioRepository.existsByCodeAndActive(request.getCode().toUpperCase(), null)) {
            throw new AppException(ErrorCode.STUDIO_CODE_ALREADY_EXISTS, 
                "Studio code already exists: " + request.getCode());
        }

        logger.debug("Studio creation request validation passed for: {}", request.getName());
    }

    /**
     * Validate and fetch postal code reference
     */
    private PostalCodeReferenceEntity validateAndFetchPostalCode(String postalCodeId) {
        PostalCodeReferenceEntity pcr = postalCodeRepository.findById(postalCodeId)
                .orElseThrow(() -> new AppException(ErrorCode.POSTAL_CODE_NOT_FOUND,
                    "Postal code not found: " + postalCodeId));
        if (pcr.getDeletedAt() != null || (pcr.getStatus() != null && !"active".equalsIgnoreCase(pcr.getStatus()))) {
            throw new AppException(ErrorCode.POSTAL_CODE_NOT_FOUND, "Postal code not active: " + postalCodeId);
        }
        return pcr;
    }

    /**
     * Validate and fetch owner (with scope-based validation)
     */
    private UserEntity validateAndFetchOwner(String ownerId, String currentUserId) {
        UserEntity owner = userRepository.findByIdAndDeletedAtIsNull(ownerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND,
                    "Owner not found: " + ownerId));

        // TODO: Add scope-based validation here based on RBAC
        // For now, allow any active user to be an owner
        // In the future, add business rules like:
        // - Current user must have permission to assign owners
        // - Owner must be in the same organization/scope
        // - Owner must have appropriate roles

        return owner;
    }

    /**
     * Create address entity from request
     */
    private AddressEntity createAddress(StudioCreateRequest.AddressCreateRequest addressRequest, 
                                      PostalCodeReferenceEntity postalCodeRef, String currentUserId) {
        
        // Auto-populate city from postal code if not provided
        String city = addressRequest.getCity();
        if (city == null || city.trim().isEmpty()) {
            city = postalCodeRef.getCity(); // Use postal code's city if available
        }

        AddressEntity address = new AddressEntity();
address.setId(Ulids.newUlid());
        address.setStreetLine1(addressRequest.getStreetLine1().trim());
        address.setStreetLine2(addressRequest.getStreetLine2() != null ? 
            addressRequest.getStreetLine2().trim() : null);
        address.setCity(city);
        address.setPostalCodeReference(postalCodeRef);
        address.setProvinceCode(postalCodeRef.getProvinceCode());
        address.setCountryCode(postalCodeRef.getCountryCode());
        address.setAttention(addressRequest.getAttention());
        address.setStatus(AddressEntity.AddressStatus.ACTIVE);
        address.setCreatedBy(currentUserId);

        // Check for similar addresses (optional warning, but allow creation)
        boolean similarExists = addressRepository.existsSimilarAddress(
            addressRequest.getStreetLine1(), 
            addressRequest.getStreetLine2(), 
            postalCodeRef.getId(), 
            null);
        
        if (similarExists) {
            logger.warn("Similar address already exists for postal code: {}, street: {}", 
                postalCodeRef.getPostalCode(), addressRequest.getStreetLine1());
        }

        AddressEntity savedAddress = addressRepository.save(address);
        logger.debug("Created address with ID: {} for postal code: {}", 
            savedAddress.getId(), postalCodeRef.getPostalCode());

        return savedAddress;
    }

    /**
     * Create studio entity from request
     */
    private StudioEntity createStudioEntity(StudioCreateRequest request, AddressEntity address, 
                                           UserEntity owner, String currentUserId) {
        StudioEntity studio = new StudioEntity();
studio.setId(Ulids.newUlid());
        studio.setName(request.getName().trim());
        studio.setCode(request.getCode().toUpperCase().trim());
        studio.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        studio.setEmail(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null);
        studio.setCompanyName(request.getCompanyName().trim());
        studio.setCompanyRegistrationNo(request.getCompanyRegistrationNo() != null ? 
            request.getCompanyRegistrationNo().trim() : null);
        studio.setAddress(address);
        studio.setOwner(owner);
        studio.setNote(request.getNote());
        studio.setStatus(StudioEntity.StudioStatus.ACTIVE);
        studio.setCreatedBy(currentUserId);

        return studio;
    }

    /**
     * Convert entity to response DTO
     */
    private StudioResponse convertToStudioResponse(StudioEntity studio) {
        StudioResponse response = new StudioResponse();
        response.setId(studio.getId());
        response.setName(studio.getName());
        response.setCode(studio.getCode());
        response.setPhone(studio.getPhone());
        response.setEmail(studio.getEmail());
        response.setCompanyName(studio.getCompanyName());
        response.setCompanyRegistrationNo(studio.getCompanyRegistrationNo());
        response.setNote(studio.getNote());
        response.setStatus(studio.getStatus().getValue());
        if (studio.getCreatedAt() != null) {
            response.setCreatedAt(OffsetDateTime.ofInstant(studio.getCreatedAt(), ZoneOffset.UTC));
        }
        if (studio.getUpdatedAt() != null) {
            response.setUpdatedAt(OffsetDateTime.ofInstant(studio.getUpdatedAt(), ZoneOffset.UTC));
        }

        // Convert owner
        if (studio.getOwner() != null) {
            StudioResponse.OwnerResponse owner = new StudioResponse.OwnerResponse();
            owner.setId(studio.getOwner().getId());
            owner.setFullName(studio.getOwner().getFullName());
            owner.setEmail(studio.getOwner().getEmail());
            owner.setPhone(studio.getOwner().getPhone());
            response.setOwner(owner);
        }

        // Convert address
        if (studio.getAddress() != null) {
            AddressEntity address = studio.getAddress();
            StudioResponse.AddressResponse addressResponse = new StudioResponse.AddressResponse();
            addressResponse.setId(address.getId());
            addressResponse.setStreetLine1(address.getStreetLine1());
            addressResponse.setStreetLine2(address.getStreetLine2());
            addressResponse.setCity(address.getCity());
            addressResponse.setAttention(address.getAttention());
            addressResponse.setFullAddress(address.getFullAddress());

            // Convert postal code
            if (address.getPostalCodeReference() != null) {
                PostalCodeReferenceEntity pcr = address.getPostalCodeReference();
                StudioResponse.PostalCodeResponse postalCodeResponse = new StudioResponse.PostalCodeResponse();
                postalCodeResponse.setId(pcr.getId());
                postalCodeResponse.setPostalCode(pcr.getPostalCode());
                postalCodeResponse.setCity(pcr.getCity());
                postalCodeResponse.setProvinceCode(pcr.getProvinceCode());
                postalCodeResponse.setCountryCode(pcr.getCountryCode());
                // TODO: Add province and country names from lookup tables
                addressResponse.setPostalCode(postalCodeResponse);
            }

            response.setAddress(addressResponse);
        }

        return response;
    }
}