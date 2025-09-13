# Test script to debug Singapore postal code processing
# Process only first 10 records for debugging

param(
    [string]$InputFile = "C:\d\1.code-myc-2025.9.12.ai\docs\backend\data dictionary\SG.txt"
)

Write-Host "Test: Processing first 10 records only" -ForegroundColor Yellow

$PostalCodes = @{}
$recordsToProcess = 10
$processed = 0

Get-Content $InputFile | ForEach-Object {
    if ($processed -ge $recordsToProcess) { return }
    
    $processed++
    $columns = $_ -split "`t"
    
    Write-Host "Record $processed`: $($columns.Count) columns" -ForegroundColor Cyan
    Write-Host "  Country: '$($columns[0])'" -ForegroundColor Gray
    Write-Host "  PostalCode: '$($columns[1])'" -ForegroundColor Gray
    Write-Host "  Street: '$($columns[2])'" -ForegroundColor Gray
    
    $uniqueKey = "$($columns[1].Trim())|$($columns[2].Trim())"
    $PostalCodes[$uniqueKey] = @{
        PostalCode = $columns[1].Trim()
        City = $columns[2].Trim()
        ProvinceCode = 'SG'
        CountryCode = $columns[0].Trim()
    }
}

Write-Host "`nHashtable contains $($PostalCodes.Count) items" -ForegroundColor Green

# Test array conversion
$PostalCodesArray = @($PostalCodes.Values)
Write-Host "Array contains $($PostalCodesArray.Count) items" -ForegroundColor Green

# Test first item
if ($PostalCodesArray.Count -gt 0) {
    $first = $PostalCodesArray[0]
    Write-Host "First item:" -ForegroundColor Yellow
    Write-Host "  PostalCode: $($first.PostalCode)" -ForegroundColor Gray
    Write-Host "  City: $($first.City)" -ForegroundColor Gray
    Write-Host "  ProvinceCode: $($first.ProvinceCode)" -ForegroundColor Gray
    Write-Host "  CountryCode: $($first.CountryCode)" -ForegroundColor Gray
}