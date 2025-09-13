# PowerShell script to process Singapore postal code data
# Processes GeoNames SG.txt file and generates SQL migration

param(
    [string]$InputFile = "C:\d\1.code-myc-2025.9.12.ai\docs\backend\data dictionary\SG.txt",
    [string]$OutputFile = "C:\d\1.code-myc-2025.9.12.ai\backend\web\src\main\resources\db\migration\V16__seed_singapore_postal_codes.sql"
)

Write-Host "Singapore Postal Code Data Processor" -ForegroundColor Cyan
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
$PostalCodes = @{}  # Use hashtable for deduplication
$StreetsFound = @{}

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
        $streetName = $columns[2].Trim()
        
        # Validate country code
        if ($countryCode -ne 'SG') {
            Write-Host "Warning: Row $TotalRecords not Singapore: $countryCode" -ForegroundColor Yellow
            $InvalidRecords++
            return
        }
        
        # Validate postal code format (6 digits)
        if ($postalCode -notmatch '^\d{6}$') {
            Write-Host "Warning: Row $TotalRecords invalid postal code format: $postalCode" -ForegroundColor Yellow
            $InvalidRecords++
            return
        }
        
        # Clean street/area name
        $streetName = $streetName.Trim()
        if ($streetName.Length -gt 120) {
            $streetName = $streetName.Substring(0, 117) + "..."
        }
        if ([string]::IsNullOrWhiteSpace($streetName)) {
            $streetName = $null
        }
        
        # Create unique key for deduplication (postal code + street name)
        $uniqueKey = "$postalCode|$streetName"
        
        # Add to postal codes list only if not already exists
        if (-not $PostalCodes.ContainsKey($uniqueKey)) {
            $PostalCodes[$uniqueKey] = @{
                PostalCode = $postalCode
                City = $streetName
                ProvinceCode = 'SG'  # Singapore uses SG as province code
                CountryCode = $countryCode
            }
            $ValidRecords++
            
            if ($streetName) {
                $StreetsFound[$streetName] = $true
            }
        }
        
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
Write-Host "Unique postal codes: $($PostalCodes.Count.ToString('N0'))" -ForegroundColor Green
$successRate = if ($TotalRecords -gt 0) { ($ValidRecords / $TotalRecords * 100) } else { 0 }
Write-Host "Success rate: $($successRate.ToString('F1'))%" -ForegroundColor White
Write-Host "Unique streets/areas: $($StreetsFound.Keys.Count.ToString('N0'))" -ForegroundColor White

Write-Host "`nReady to generate SQL with $($PostalCodes.Count.ToString('N0')) postal codes" -ForegroundColor Green

if ($PostalCodes.Count -eq 0) {
    Write-Host "Error: No postal codes to process." -ForegroundColor Red
    exit 1
}

# Generate SQL migration
Write-Host "`nGenerating SQL migration: $OutputFile" -ForegroundColor Green

# Create migration content
$migrationContent = @"
-- V16__seed_singapore_postal_codes.sql
-- Seed Singapore postal codes from GeoNames data
-- Generated automatically from SG.txt

-- Create temporary ULID generation function
CREATE OR REPLACE FUNCTION temp_generate_ulid() RETURNS CHAR(26) AS `$`$
DECLARE
    timestamp_part BIGINT;
    random_part TEXT;
    ulid TEXT;
BEGIN
    timestamp_part := EXTRACT(EPOCH FROM NOW()) * 1000;
    random_part := UPPER(SUBSTR(MD5(RANDOM()::TEXT || RANDOM()::TEXT), 1, 16));
    random_part := REPLACE(random_part, 'D', '8');
    random_part := REPLACE(random_part, 'I', '9');
    random_part := REPLACE(random_part, 'L', 'K');
    random_part := REPLACE(random_part, 'O', '0');
    random_part := REPLACE(random_part, 'U', 'V');
    
    ulid := '0' || LPAD(ABS(HASHTEXT(timestamp_part::TEXT))::TEXT, 9, '0') || random_part;
    RETURN SUBSTR(ulid, 1, 26);
END;
`$`$ LANGUAGE plpgsql;

-- Insert Singapore postal codes

"@

# Convert hashtable to array for processing
$PostalCodesArray = @($PostalCodes.Values)
Write-Host "Debug: PostalCodesArray count = $($PostalCodesArray.Count)" -ForegroundColor Magenta

# Add postal codes in batches
$batchSize = 500
$totalBatches = [Math]::Ceiling($PostalCodesArray.Count / $batchSize)
Write-Host "Debug: Will create $totalBatches batches of $batchSize records each" -ForegroundColor Magenta

for ($batchNum = 0; $batchNum -lt $totalBatches; $batchNum++) {
    $startIdx = $batchNum * $batchSize
    $endIdx = [Math]::Min($startIdx + $batchSize, $PostalCodesArray.Count)
    $batch = $PostalCodesArray[$startIdx..($endIdx-1)]
    
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
            $migrationContent += "`n"
        }
    }
    
    $migrationContent += "ON CONFLICT ON CONSTRAINT ux_postal_city_country DO NOTHING;`n"
}

# Add footer
$migrationContent += @"

-- Drop temporary function
DROP FUNCTION IF EXISTS temp_generate_ulid();

-- Add indexes for performance (if not already exist)
CREATE INDEX IF NOT EXISTS ix_postal_code_sg_lookup 
ON postal_code_reference (country_code, postal_code) 
WHERE country_code = 'SG' AND status = 'active';

-- Verification queries
-- SELECT COUNT(*) FROM postal_code_reference WHERE country_code = 'SG';
-- SELECT LEFT(postal_code, 2) as sector, COUNT(*) FROM postal_code_reference WHERE country_code = 'SG' GROUP BY LEFT(postal_code, 2) ORDER BY LEFT(postal_code, 2);

-- Migration complete: $($PostalCodesArray.Count.ToString('N0')) Singapore postal codes added
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
    Write-Host "Contains $($PostalCodesArray.Count.ToString('N0')) postal code inserts" -ForegroundColor Green
    
    Write-Host "`nProcessing completed successfully!" -ForegroundColor Green
    Write-Host "1. Review the generated SQL file" -ForegroundColor Gray
    Write-Host "2. Run the migration: ./mvnw flyway:migrate" -ForegroundColor Gray
    Write-Host "3. Verify data: SELECT COUNT(*) FROM postal_code_reference WHERE country_code = 'SG';" -ForegroundColor Gray
    
} catch {
    Write-Host "Error writing output file: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}