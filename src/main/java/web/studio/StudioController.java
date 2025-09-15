package web.studio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.common.request.RequestIdHolder;
import web.studio.dto.StudioCreateRequest;
import web.studio.dto.StudioResponse;

import java.util.List;

@RestController
@RequestMapping("/api/studios")
@Tag(name = "Studio Management", description = "APIs for managing dance studios")
public class StudioController {

    private static final Logger logger = LoggerFactory.getLogger(StudioController.class);

    private final StudioService studioService;

    @Autowired
    public StudioController(StudioService studioService) {
        this.studioService = studioService;
    }

    @PostMapping
    @Operation(summary = "Create a new studio", description = "Creates a new dance studio with associated address and owner information")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Studio created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "409", description = "Studio email or code already exists"),
        @ApiResponse(responseCode = "422", description = "Validation failed")
    })
    public ResponseEntity<web.common.dto.ApiResponse<StudioResponse>> createStudio(
            @Valid @RequestBody StudioCreateRequest request) {
        
        String currentUserId = "system"; // TODO: replace with authenticated user id when available
        logger.info("Creating studio: {} by user: {}", request.getName(), currentUserId);

        StudioResponse studio = studioService.createStudio(request, currentUserId);
        
        logger.info("Successfully created studio with ID: {}", studio.getId());
        String rid = RequestIdHolder.getOrCreate();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(web.common.dto.ApiResponse.success(rid, studio, null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get studio by ID", description = "Retrieves a studio by its unique identifier")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Studio found"),
        @ApiResponse(responseCode = "404", description = "Studio not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<web.common.dto.ApiResponse<StudioResponse>> getStudioById(
            @Parameter(description = "Studio ID", required = true) @PathVariable String id) {
        
        logger.debug("Fetching studio by ID: {}", id);
        StudioResponse studio = studioService.findStudioById(id);
        
        String rid = RequestIdHolder.getOrCreate();
        return ResponseEntity.ok(web.common.dto.ApiResponse.success(rid, studio, null));
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get studios by owner", description = "Retrieves all studios owned by a specific user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Studios found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<web.common.dto.ApiResponse<List<StudioResponse>>> getStudiosByOwner(
            @Parameter(description = "Owner user ID", required = true) @PathVariable String ownerId) {
        
        logger.debug("Fetching studios by owner: {}", ownerId);
        List<StudioResponse> studios = studioService.findStudiosByOwnerId(ownerId);
        
        String rid = RequestIdHolder.getOrCreate();
        return ResponseEntity.ok(web.common.dto.ApiResponse.success(rid, studios, null));
    }

    @GetMapping("/owner/{ownerId}/paginated")
    @Operation(summary = "Get studios by owner with pagination", description = "Retrieves studios owned by a specific user with pagination support")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Studios found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<web.common.dto.ApiResponse<Page<StudioResponse>>> getStudiosByOwnerPaginated(
            @Parameter(description = "Owner user ID", required = true) @PathVariable String ownerId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir) {
        
        logger.debug("Fetching studios by owner with pagination: {} (page: {}, size: {})", ownerId, page, size);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<StudioResponse> studioPage = studioService.findStudiosByOwner(ownerId, pageable);
        
        String rid = RequestIdHolder.getOrCreate();
        return ResponseEntity.ok(web.common.dto.ApiResponse.success(rid, studioPage, null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search studios by name", description = "Searches for studios using full-text search on studio names")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search completed"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<web.common.dto.ApiResponse<List<StudioResponse>>> searchStudiosByName(
            @Parameter(description = "Search term", required = true) @RequestParam String q,
            @Parameter(description = "Maximum number of results") @RequestParam(defaultValue = "20") int limit) {
        
        logger.debug("Searching studios by name: {} (limit: {})", q, limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<StudioResponse> studios = studioService.searchStudiosByName(q, pageable);
        
        String rid = RequestIdHolder.getOrCreate();
        return ResponseEntity.ok(web.common.dto.ApiResponse.success(rid, studios, null));
    }

    @GetMapping("/geographic")
    @Operation(summary = "Find studios by geographic area", description = "Finds studios in a specific country and optionally province")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Studios found"),
        @ApiResponse(responseCode = "400", description = "Invalid geographic parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<web.common.dto.ApiResponse<List<StudioResponse>>> getStudiosByGeographicArea(
            @Parameter(description = "Country code (ISO 3166-1 alpha-2)", required = true) @RequestParam String countryCode,
            @Parameter(description = "Province code (optional)") @RequestParam(required = false) String provinceCode,
            @Parameter(description = "Maximum number of results") @RequestParam(defaultValue = "50") int limit) {
        
        logger.debug("Fetching studios by geographic area: {} / {} (limit: {})", countryCode, provinceCode, limit);
        
        Pageable pageable = PageRequest.of(0, limit, Sort.by("name"));
        List<StudioResponse> studios = studioService.findStudiosByGeographicArea(countryCode, provinceCode, pageable);
        
        String rid = RequestIdHolder.getOrCreate();
        return ResponseEntity.ok(web.common.dto.ApiResponse.success(rid, studios, null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my studios", description = "Retrieves all studios owned by the current authenticated user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Studios found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<web.common.dto.ApiResponse<List<StudioResponse>>> getMyStudios() {
        
        String currentUserId = "system"; // TODO: replace with authenticated user id when available
        logger.debug("Fetching studios for current user: {}", currentUserId);
        List<StudioResponse> studios = studioService.findStudiosByOwnerId(currentUserId);
        
        String rid = RequestIdHolder.getOrCreate();
        return ResponseEntity.ok(web.common.dto.ApiResponse.success(rid, studios, null));
    }

    // TODO: Add additional endpoints as needed:
    // - PUT /api/studios/{id} - Update studio
    // - DELETE /api/studios/{id} - Delete/archive studio  
    // - GET /api/studios - List all studios (with pagination and filtering)
    // - POST /api/studios/{id}/activate - Activate studio
    // - POST /api/studios/{id}/suspend - Suspend studio
}