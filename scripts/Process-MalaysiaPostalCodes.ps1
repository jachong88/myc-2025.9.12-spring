# PowerShell script to process Malaysia postal code data
# Processes GeoNames MY.txt file and generates SQL migration

param(
    [string]$InputFile = "C:\Users\jacho\OneDrive\Desktop\MY.txt",
    [string]$OutputFile = "C:\d\1.code-myc-2025.9.12.ai\backend\web\src\main\resources\db\migration\V14__seed_malaysia_postal_codes.sql"
)

# State code mapping from GeoNames 3-letter codes to ISO 3166-2 codes
$StateMapping = @{
    'JHR' = 'MY-01'  # Johor
    'KDH' = 'MY-02'  # Kedah
    'KTN' = 'MY-03'  # Kelantan
    'MLK' = 'MY-04'  # Melaka
    'NSN' = 'MY-05'  # Negeri Sembilan
    'PHG' = 'MY-06'  # Pahang
    'PNG' = 'MY-07'  # Penang
    'PRK' = 'MY-08'  # Perak
    'PLS' = 'MY-09'  # Perlis
    'SGR' = 'MY-10'  # Selangor
    'SBH' = 'MY-11'  # Sabah
    'SWK' = 'MY-12'  # Sarawak
    'TRG' = 'MY-13'  # Terengganu
    'KUL' = 'MY-14'  # Kuala Lumpur
    'LBN' = 'MY-15'  # Labuan
    'PJY' = 'MY-16'  # Putrajaya
    'SRW' = 'MY-12'  # Sarawak (alternate code)
}

Write-Host "Malaysia Postal Code Data Processor" -ForegroundColor Cyan
Write-Host "=" * 40 -ForegroundColor Cyan
Write-Host "Input file: $InputFile" -ForegroundColor Yellow
Write-Host "Output file: $OutputFile" -ForegroundColor Yellow

# Check if input file exists
if (-not (Test-Path $InputFile)) {
    Write-Host "Error: Input file not found: $InputFile" -ForegroundColor Red
    exit 1
}

# Initialize counters
$TotalRecords = 0
$ValidRecords = 0
$InvalidRecords = 0
$PostalCodes = @()
$StatesFound = @{}
$UnmappedStates = @{}

Write-Host "`nProcessing $InputFile..." -ForegroundColor Green

# Process the file
Get-Content $InputFile | ForEach-Object {
    $TotalRecords++
    $columns = $_ -split "`t"
    
    if ($columns.Count -ne 12) {
        Write-Host "Warning: Row $TotalRecords has unexpected column count: $($columns.Count), expected 12" -ForegroundColor Yellow
        $InvalidRecords++
        return
    }
    
    try {
        $countryCode = $columns[0].Trim()
        $postalCode = $columns[1].Trim()
        $city = $columns[2].Trim()
        $stateName = $columns[3].Trim()
        $stateCode3Letter = $columns[4].Trim()
        
        # Validate country code
        if ($countryCode -ne 'MY') {
            Write-Host "Warning: Row $TotalRecords not Malaysia: $countryCode" -ForegroundColor Yellow
            $InvalidRecords++
            return
        }
        
        # Validate postal code format (5 digits)
        if ($postalCode -notmatch '^\d{5}$') {
            Write-Host "Warning: Row $TotalRecords invalid postal code format: $postalCode" -ForegroundColor Yellow
            $InvalidRecords++
            return
        }
        
        # Map state code
        $isoProvinceCode = $StateMapping[$stateCode3Letter]
        if (-not $isoProvinceCode) {
            Write-Host "Warning: Row $TotalRecords unmapped state code: $stateCode3Letter ($stateName)" -ForegroundColor Yellow
            $UnmappedStates["${stateCode3Letter}:${stateName}"] = $true
            $InvalidRecords++
            return
        }
        
        # Clean city name
        $city = $city.Trim()
        if ($city.Length -gt 120) {
            $city = $city.Substring(0, 117) + "..."
        }
        if ([string]::IsNullOrWhiteSpace($city)) {
            $city = $null
        }
        
        # Add to postal codes list
        $PostalCodes += @{
            PostalCode = $postalCode
            City = $city
            ProvinceCode = $isoProvinceCode
            CountryCode = $countryCode
        }
        
        $ValidRecords++
        $StatesFound["${isoProvinceCode}:${stateName}"] = $true
        
    } catch {
        Write-Host "Error processing row $TotalRecords`: $($_.Exception.Message)" -ForegroundColor Red
        $InvalidRecords++
    }
}

# Print statistics
Write-Host "`n" + ("=" * 50) -ForegroundColor Cyan
Write-Host "PROCESSING STATISTICS" -ForegroundColor Cyan
Write-Host ("=" * 50) -ForegroundColor Cyan
Write-Host "Total records processed: $($TotalRecords.ToString('N0'))" -ForegroundColor White
Write-Host "Valid records: $($ValidRecords.ToString('N0'))" -ForegroundColor Green
Write-Host "Invalid records: $($InvalidRecords.ToString('N0'))" -ForegroundColor Red
$successRate = if ($TotalRecords -gt 0) { ($ValidRecords / $TotalRecords * 100) } else { 0 }
Write-Host "Success rate: $($successRate.ToString('F1'))%" -ForegroundColor White

Write-Host "`nStates found ($($StatesFound.Keys.Count)):" -ForegroundColor White
$StatesFound.Keys | Sort-Object | ForEach-Object {
    Write-Host "  - $_" -ForegroundColor Gray
}

if ($UnmappedStates.Keys.Count -gt 0) {
    Write-Host "`nUnmapped states ($($UnmappedStates.Keys.Count)):" -ForegroundColor Yellow
    $UnmappedStates.Keys | Sort-Object | ForEach-Object {
        Write-Host "  - $_" -ForegroundColor Yellow
    }
}

Write-Host "`nReady to generate SQL with $($ValidRecords.ToString('N0')) postal codes" -ForegroundColor Green

if ($PostalCodes.Count -eq 0) {
    Write-Host "Error: No postal codes to process." -ForegroundColor Red
    exit 1
}

# Generate SQL migration
Write-Host "`nGenerating SQL migration: $OutputFile" -ForegroundColor Green

# Create migration content
$migrationContent = @"
-- V14__seed_malaysia_postal_codes.sql
-- Seed Malaysian postal codes from GeoNames data
-- Generated automatically from MY.txt

-- Create temporary ULID generation function
CREATE OR REPLACE FUNCTION temp_generate_ulid() RETURNS CHAR(26) AS `$`$
DECLARE
    timestamp_part BIGINT;
    random_part TEXT;
    ulid TEXT;
BEGIN
    timestamp_part := EXTRACT(EPOCH FROM NOW()) * 1000;
    random_part := UPPER(SUBSTR(MD5(RANDOM()::TEXT), 1, 16));
    ulid := LPAD(TO_HEX(timestamp_part), 10, '0') || random_part;
    RETURN SUBSTR(ulid, 1, 26);
END;
`$`$ LANGUAGE plpgsql;

-- Insert Malaysian postal codes

"@

# Add postal codes in batches
$batchSize = 500
$totalBatches = [Math]::Ceiling($PostalCodes.Count / $batchSize)

for ($batchNum = 0; $batchNum -lt $totalBatches; $batchNum++) {
    $startIdx = $batchNum * $batchSize
    $endIdx = [Math]::Min($startIdx + $batchSize, $PostalCodes.Count)
    $batch = $PostalCodes[$startIdx..($endIdx-1)]
    
    $migrationContent += "`n-- Batch $($batchNum + 1)/$totalBatches ($($batch.Count) records)`n"
    $migrationContent += "INSERT INTO postal_code_reference (id, postal_code, city, province_code, country_code, status, created_at, created_by, updated_at)`n"
    $migrationContent += "VALUES`n"
    
    for ($i = 0; $i -lt $batch.Count; $i++) {
        $item = $batch[$i]
        $cityValue = if ($item.City) { "'$($item.City.Replace("'", "''"))'" } else { "NULL" }
        
        $migrationContent += "  (temp_generate_ulid(), '$($item.PostalCode)', $cityValue, '$($item.ProvinceCode)', '$($item.CountryCode)', 'active', NOW(), 'system', NOW())"
        
        if ($i -lt ($batch.Count - 1)) {
            $migrationContent += ",`n"
        } else {
            $migrationContent += ";`n"
        }
    }
}

# Add footer
$migrationContent += @"

-- Drop temporary function
DROP FUNCTION IF EXISTS temp_generate_ulid();

-- Add indexes for performance (if not already exist)
CREATE INDEX IF NOT EXISTS ix_postal_code_my_lookup 
ON postal_code_reference (country_code, postal_code) 
WHERE country_code = 'MY' AND status = 'active';

-- Verification queries
-- SELECT COUNT(*) FROM postal_code_reference WHERE country_code = 'MY';
-- SELECT province_code, COUNT(*) FROM postal_code_reference WHERE country_code = 'MY' GROUP BY province_code ORDER BY province_code;

-- Migration complete: $($PostalCodes.Count.ToString('N0')) Malaysian postal codes added
"@

# Write to file
try {
    # Create directory if it doesn't exist
    $outputDir = Split-Path $OutputFile -Parent
    if (-not (Test-Path $outputDir)) {
        New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
    }
    
    # Write the migration file
    $migrationContent | Out-File -FilePath $OutputFile -Encoding UTF8
    
    Write-Host "SQL migration generated: $OutputFile" -ForegroundColor Green
    Write-Host "Contains $($PostalCodes.Count.ToString('N0')) postal code inserts" -ForegroundColor Green
    
    Write-Host "`nProcessing completed successfully!" -ForegroundColor Green
    Write-Host "1. Review the generated SQL file" -ForegroundColor Gray
    Write-Host "2. Run the migration: ./mvnw flyway:migrate" -ForegroundColor Gray
    Write-Host "3. Verify data: SELECT COUNT(*) FROM postal_code_reference WHERE country_code = 'MY';" -ForegroundColor Gray
    
} catch {
    Write-Host "Error writing output file: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}