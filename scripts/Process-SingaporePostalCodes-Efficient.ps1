# PowerShell script to process Singapore postal code data - Memory Efficient Version
# Processes GeoNames SG.txt file and generates SQL migration

param(
    [string]$InputFile = "C:\d\1.code-myc-2025.9.12.ai\docs\backend\data dictionary\SG.txt",
    [string]$OutputFile = "C:\d\1.code-myc-2025.9.12.ai\backend\web\src\main\resources\db\migration\V16__seed_singapore_postal_codes.sql"
)

Write-Host "Singapore Postal Code Data Processor (Memory Efficient)" -ForegroundColor Cyan
Write-Host "=" * 60 -ForegroundColor Cyan
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

# Process the file and build hashtable
Get-Content $InputFile | ForEach-Object {
    $TotalRecords++
    $columns = $_ -split "`t"
    
    # Progress indicator
    if ($TotalRecords % 10000 -eq 0) {
        Write-Host "  Processed $($TotalRecords.ToString('N0')) records..." -ForegroundColor Gray
    }
    
    if ($columns.Count -ne 12) {
        $InvalidRecords++
        return
    }
    
    try {
        $countryCode = $columns[0].Trim()
        $postalCode = $columns[1].Trim()
        $streetName = $columns[2].Trim()
        
        # Validate country code and postal code format
        if ($countryCode -ne 'SG' -or $postalCode -notmatch '^\d{6}$') {
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
        
        # Create unique key for deduplication
        $uniqueKey = "$postalCode|$streetName"
        
        # Add to postal codes list only if not already exists
        if (-not $PostalCodes.ContainsKey($uniqueKey)) {
            $PostalCodes[$uniqueKey] = @{
                PostalCode = $postalCode
                City = $streetName
                ProvinceCode = 'SG'
                CountryCode = $countryCode
            }
            $ValidRecords++
            
            if ($streetName) {
                $StreetsFound[$streetName] = $true
            }
        }
        
    } catch {
        $InvalidRecords++
    }
}

# Print statistics
Write-Host "`n" + ("=" * 60) -ForegroundColor Cyan
Write-Host "PROCESSING STATISTICS" -ForegroundColor Cyan
Write-Host ("=" * 60) -ForegroundColor Cyan
Write-Host "Total records processed: $($TotalRecords.ToString('N0'))" -ForegroundColor White
Write-Host "Valid records: $($ValidRecords.ToString('N0'))" -ForegroundColor Green
Write-Host "Invalid records: $($InvalidRecords.ToString('N0'))" -ForegroundColor Red
Write-Host "Unique postal codes: $($PostalCodes.Count.ToString('N0'))" -ForegroundColor Green
$successRate = if ($TotalRecords -gt 0) { ($ValidRecords / $TotalRecords * 100) } else { 0 }
Write-Host "Success rate: $($successRate.ToString('F1'))%" -ForegroundColor White
Write-Host "Unique streets/areas: $($StreetsFound.Keys.Count.ToString('N0'))" -ForegroundColor White

if ($PostalCodes.Count -eq 0) {
    Write-Host "Error: No postal codes to process." -ForegroundColor Red
    exit 1
}

# Generate SQL migration by writing directly to file
Write-Host "`nGenerating SQL migration: $OutputFile" -ForegroundColor Green

try {
    # Create directory if it doesn't exist
    $outputDir = Split-Path $OutputFile -Parent
    if (-not (Test-Path $outputDir)) {
        New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
    }

    # Open file for writing
    $writer = [System.IO.StreamWriter]::new($OutputFile, $false, [System.Text.Encoding]::UTF8)
    
    # Write header
    $writer.WriteLine("-- V16__seed_singapore_postal_codes.sql")
    $writer.WriteLine("-- Seed Singapore postal codes from GeoNames data")
    $writer.WriteLine("-- Generated automatically from SG.txt")
    $writer.WriteLine("")
    $writer.WriteLine("-- Create temporary ULID generation function")
    $writer.WriteLine("CREATE OR REPLACE FUNCTION temp_generate_ulid() RETURNS CHAR(26) AS `$`$")
    $writer.WriteLine("DECLARE")
    $writer.WriteLine("    timestamp_part BIGINT;")
    $writer.WriteLine("    random_part TEXT;")
    $writer.WriteLine("    ulid TEXT;")
    $writer.WriteLine("BEGIN")
    $writer.WriteLine("    timestamp_part := EXTRACT(EPOCH FROM NOW()) * 1000;")
    $writer.WriteLine("    random_part := UPPER(SUBSTR(MD5(RANDOM()::TEXT || RANDOM()::TEXT), 1, 16));")
    $writer.WriteLine("    random_part := REPLACE(random_part, 'D', '8');")
    $writer.WriteLine("    random_part := REPLACE(random_part, 'I', '9');")
    $writer.WriteLine("    random_part := REPLACE(random_part, 'L', 'K');")
    $writer.WriteLine("    random_part := REPLACE(random_part, 'O', '0');")
    $writer.WriteLine("    random_part := REPLACE(random_part, 'U', 'V');")
    $writer.WriteLine("    ")
    $writer.WriteLine("    ulid := '0' || LPAD(ABS(HASHTEXT(timestamp_part::TEXT))::TEXT, 9, '0') || random_part;")
    $writer.WriteLine("    RETURN SUBSTR(ulid, 1, 26);")
    $writer.WriteLine("END;")
    $writer.WriteLine("`$`$ LANGUAGE plpgsql;")
    $writer.WriteLine("")
    $writer.WriteLine("-- Insert Singapore postal codes")
    $writer.WriteLine("")

    # Convert to array and process in batches
    $PostalCodesArray = @($PostalCodes.Values)
    $batchSize = 500
    $totalBatches = [Math]::Ceiling($PostalCodesArray.Count / $batchSize)
    
    Write-Host "Writing $($PostalCodesArray.Count.ToString('N0')) postal codes in $totalBatches batches..." -ForegroundColor Green
    
    for ($batchNum = 0; $batchNum -lt $totalBatches; $batchNum++) {
        $startIdx = $batchNum * $batchSize
        $endIdx = [Math]::Min($startIdx + $batchSize, $PostalCodesArray.Count)
        $batch = $PostalCodesArray[$startIdx..($endIdx-1)]
        
        # Progress indicator
        if (($batchNum + 1) % 50 -eq 0) {
            Write-Host "  Writing batch $($batchNum + 1)/$totalBatches..." -ForegroundColor Gray
        }
        
        # Write batch header
        $writer.WriteLine("-- Batch $($batchNum + 1)/$totalBatches ($($batch.Count) records)")
        $writer.WriteLine("INSERT INTO postal_code_reference (id, postal_code, city, province_code, country_code, status, created_at, created_by, updated_at)")
        $writer.WriteLine("VALUES")
        
        # Write each record in the batch
        for ($i = 0; $i -lt $batch.Count; $i++) {
            $item = $batch[$i]
            $cityValue = if ($item.City) { "'$($item.City.Replace("'", "''"))'" } else { "NULL" }
            
            $line = "  (temp_generate_ulid(), '$($item.PostalCode)', $cityValue, '$($item.ProvinceCode)', '$($item.CountryCode)', 'active', NOW(), 'system', NOW())"
            
            if ($i -lt ($batch.Count - 1)) {
                $line += ","
            }
            
            $writer.WriteLine($line)
        }
        
        $writer.WriteLine("ON CONFLICT ON CONSTRAINT ux_postal_city_country DO NOTHING;")
        $writer.WriteLine("")
    }
    
    # Write footer
    $writer.WriteLine("-- Drop temporary function")
    $writer.WriteLine("DROP FUNCTION IF EXISTS temp_generate_ulid();")
    $writer.WriteLine("")
    $writer.WriteLine("-- Add indexes for performance (if not already exist)")
    $writer.WriteLine("CREATE INDEX IF NOT EXISTS ix_postal_code_sg_lookup")
    $writer.WriteLine("ON postal_code_reference (country_code, postal_code)")
    $writer.WriteLine("WHERE country_code = 'SG' AND status = 'active';")
    $writer.WriteLine("")
    $writer.WriteLine("-- Verification queries")
    $writer.WriteLine("-- SELECT COUNT(*) FROM postal_code_reference WHERE country_code = 'SG';")
    $writer.WriteLine("-- SELECT LEFT(postal_code, 2) as sector, COUNT(*) FROM postal_code_reference WHERE country_code = 'SG' GROUP BY LEFT(postal_code, 2) ORDER BY LEFT(postal_code, 2);")
    $writer.WriteLine("")
    $writer.WriteLine("-- Migration complete: $($PostalCodesArray.Count.ToString('N0')) Singapore postal codes added")
    
    $writer.Close()
    
    Write-Host "SQL migration generated successfully: $OutputFile" -ForegroundColor Green
    Write-Host "Contains $($PostalCodesArray.Count.ToString('N0')) postal code inserts" -ForegroundColor Green
    
    Write-Host "`nProcessing completed successfully!" -ForegroundColor Green
    Write-Host "1. Review the generated SQL file" -ForegroundColor Gray
    Write-Host "2. Run the migration: ./mvnw flyway:migrate" -ForegroundColor Gray
    Write-Host "3. Verify data: SELECT COUNT(*) FROM postal_code_reference WHERE country_code = 'SG';" -ForegroundColor Gray
    
} catch {
    Write-Host "Error writing output file: $($_.Exception.Message)" -ForegroundColor Red
    if ($writer) { $writer.Close() }
    exit 1
}