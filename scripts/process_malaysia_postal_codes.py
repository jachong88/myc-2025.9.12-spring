#!/usr/bin/env python3
"""
Malaysia Postal Code Data Processor
Processes GeoNames MY.txt file and generates SQL migration for postal_code_reference table
"""

import csv
import sys
from pathlib import Path
from typing import Dict, List, Tuple
import re

# State code mapping from GeoNames 3-letter codes to ISO 3166-2 codes
STATE_CODE_MAPPING = {
    'JHR': 'MY-01',  # Johor
    'KDH': 'MY-02',  # Kedah
    'KTN': 'MY-03',  # Kelantan
    'MLK': 'MY-04',  # Melaka
    'NSN': 'MY-05',  # Negeri Sembilan
    'PHG': 'MY-06',  # Pahang
    'PNG': 'MY-07',  # Penang (Pulau Pinang)
    'PRK': 'MY-08',  # Perak
    'PLS': 'MY-09',  # Perlis
    'SGR': 'MY-10',  # Selangor
    'SBH': 'MY-11',  # Sabah
    'SWK': 'MY-12',  # Sarawak
    'TRG': 'MY-13',  # Terengganu
    'KUL': 'MY-14',  # Kuala Lumpur (Federal Territory)
    'LBN': 'MY-15',  # Labuan (Federal Territory)
    'PJY': 'MY-16',  # Putrajaya (Federal Territory)
}

class MalaysiaPostalCodeProcessor:
    def __init__(self, input_file: str):
        self.input_file = Path(input_file)
        self.postal_codes: List[Tuple[str, str, str, str]] = []
        self.stats = {
            'total_records': 0,
            'valid_records': 0,
            'invalid_records': 0,
            'unmapped_states': set(),
            'states_found': set()
        }

    def process_file(self) -> None:
        """Process the GeoNames MY.txt file"""
        print(f"Processing {self.input_file}...")
        
        with open(self.input_file, 'r', encoding='utf-8') as file:
            reader = csv.reader(file, delimiter='\t')
            
            for row_num, row in enumerate(reader, 1):
                self.stats['total_records'] += 1
                
                if len(row) < 13:
                    print(f"Warning: Row {row_num} has insufficient columns: {len(row)}")
                    self.stats['invalid_records'] += 1
                    continue
                
                try:
                    country_code = row[0].strip()
                    postal_code = row[1].strip()
                    city = row[2].strip()
                    state_name = row[3].strip()
                    state_code_3letter = row[4].strip()
                    
                    # Validate country code
                    if country_code != 'MY':
                        print(f"Warning: Row {row_num} not Malaysia: {country_code}")
                        self.stats['invalid_records'] += 1
                        continue
                    
                    # Validate postal code format (5 digits)
                    if not re.match(r'^\d{5}$', postal_code):
                        print(f"Warning: Row {row_num} invalid postal code format: {postal_code}")
                        self.stats['invalid_records'] += 1
                        continue
                    
                    # Map state code
                    iso_province_code = STATE_CODE_MAPPING.get(state_code_3letter)
                    if not iso_province_code:
                        print(f"Warning: Row {row_num} unmapped state code: {state_code_3letter} ({state_name})")
                        self.stats['unmapped_states'].add(f"{state_code_3letter}:{state_name}")
                        self.stats['invalid_records'] += 1
                        continue
                    
                    # Clean city name
                    city = self._clean_city_name(city)
                    
                    # Add to postal codes list
                    self.postal_codes.append((postal_code, city, iso_province_code, country_code))
                    self.stats['valid_records'] += 1
                    self.stats['states_found'].add(f"{iso_province_code}:{state_name}")
                    
                except Exception as e:
                    print(f"Error processing row {row_num}: {e}")
                    self.stats['invalid_records'] += 1
                    continue
        
        print(f"Processing complete!")
        self._print_stats()
    
    def _clean_city_name(self, city: str) -> str:
        """Clean and normalize city names"""
        if not city:
            return None
        
        # Remove extra whitespace
        city = ' '.join(city.split())
        
        # Truncate if too long (database limit is 120 chars)
        if len(city) > 120:
            city = city[:117] + "..."
        
        # Handle common encoding issues
        city = city.replace('�', '')
        
        return city if city else None
    
    def _print_stats(self) -> None:
        """Print processing statistics"""
        print("\n" + "="*50)
        print("PROCESSING STATISTICS")
        print("="*50)
        print(f"Total records processed: {self.stats['total_records']:,}")
        print(f"Valid records: {self.stats['valid_records']:,}")
        print(f"Invalid records: {self.stats['invalid_records']:,}")
        print(f"Success rate: {(self.stats['valid_records'] / self.stats['total_records'] * 100):.1f}%")
        
        print(f"\nStates found ({len(self.stats['states_found'])}):")
        for state in sorted(self.stats['states_found']):
            print(f"  - {state}")
        
        if self.stats['unmapped_states']:
            print(f"\nUnmapped states ({len(self.stats['unmapped_states'])}):")
            for state in sorted(self.stats['unmapped_states']):
                print(f"  - {state}")
        
        print(f"\nReady to generate SQL with {self.stats['valid_records']:,} postal codes")
    
    def generate_sql_migration(self, output_file: str) -> None:
        """Generate SQL migration file"""
        if not self.postal_codes:
            print("Error: No postal codes to process. Run process_file() first.")
            return
        
        output_path = Path(output_file)
        print(f"\nGenerating SQL migration: {output_path}")
        
        with open(output_path, 'w', encoding='utf-8') as f:
            self._write_migration_header(f)
            self._write_postal_code_inserts(f)
            self._write_migration_footer(f)
        
        print(f"✅ SQL migration generated: {output_path}")
        print(f"📊 Contains {len(self.postal_codes):,} postal code inserts")
    
    def _write_migration_header(self, f) -> None:
        """Write SQL migration header"""
        f.write("""-- V14__seed_malaysia_postal_codes.sql
-- Seed Malaysian postal codes from GeoNames data
-- Generated automatically from MY.txt

-- Create temporary ULID generation function
CREATE OR REPLACE FUNCTION temp_generate_ulid() RETURNS CHAR(26) AS $$
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
$$ LANGUAGE plpgsql;

-- Insert Malaysian postal codes
""")
    
    def _write_postal_code_inserts(self, f) -> None:
        """Write postal code INSERT statements in batches"""
        batch_size = 500  # Insert in batches for better performance
        total_batches = (len(self.postal_codes) + batch_size - 1) // batch_size
        
        for batch_num in range(total_batches):
            start_idx = batch_num * batch_size
            end_idx = min(start_idx + batch_size, len(self.postal_codes))
            batch = self.postal_codes[start_idx:end_idx]
            
            f.write(f"\n-- Batch {batch_num + 1}/{total_batches} ({len(batch)} records)\n")
            f.write("INSERT INTO postal_code_reference (id, postal_code, city, province_code, country_code, status, created_at, created_by, updated_at)\n")
            f.write("VALUES\n")
            
            for i, (postal_code, city, province_code, country_code) in enumerate(batch):
                city_sql = f"'{self._escape_sql(city)}'" if city else "NULL"
                
                f.write(f"  (temp_generate_ulid(), '{postal_code}', {city_sql}, '{province_code}', '{country_code}', 'active', NOW(), 'system', NOW())")
                
                # Add comma except for last item
                if i < len(batch) - 1:
                    f.write(",\n")
                else:
                    f.write(";\n")
    
    def _write_migration_footer(self, f) -> None:
        """Write SQL migration footer"""
        f.write(f"""
-- Drop temporary function
DROP FUNCTION IF EXISTS temp_generate_ulid();

-- Add indexes for performance (if not already exist)
CREATE INDEX IF NOT EXISTS ix_postal_code_my_lookup 
ON postal_code_reference (country_code, postal_code) 
WHERE country_code = 'MY' AND status = 'active';

-- Verification queries
-- SELECT COUNT(*) FROM postal_code_reference WHERE country_code = 'MY';
-- SELECT province_code, COUNT(*) FROM postal_code_reference WHERE country_code = 'MY' GROUP BY province_code ORDER BY province_code;

-- Migration complete: {len(self.postal_codes):,} Malaysian postal codes added
""")
    
    def _escape_sql(self, text: str) -> str:
        """Escape SQL string literals"""
        if not text:
            return ""
        return text.replace("'", "''")

def main():
    """Main function"""
    print("Malaysia Postal Code Data Processor")
    print("=" * 40)
    
    # Default paths
    default_input = r"C:\Users\jacho\OneDrive\Desktop\MY.txt"
    default_output = r"C:\d\1.code-myc-2025.9.12.ai\backend\web\src\main\resources\db\migration\V14__seed_malaysia_postal_codes.sql"
    
    # Use command line args or defaults
    input_file = sys.argv[1] if len(sys.argv) > 1 else default_input
    output_file = sys.argv[2] if len(sys.argv) > 2 else default_output
    
    print(f"Input file: {input_file}")
    print(f"Output file: {output_file}")
    
    # Check if input file exists
    if not Path(input_file).exists():
        print(f"❌ Error: Input file not found: {input_file}")
        return 1
    
    try:
        # Process the data
        processor = MalaysiaPostalCodeProcessor(input_file)
        processor.process_file()
        
        # Generate SQL migration
        processor.generate_sql_migration(output_file)
        
        print("\n🎉 Processing completed successfully!")
        print("\nNext steps:")
        print("1. Review the generated SQL file")
        print("2. Run the migration: ./mvnw flyway:migrate")
        print("3. Verify data: SELECT COUNT(*) FROM postal_code_reference WHERE country_code = 'MY';")
        
        return 0
        
    except Exception as e:
        print(f"❌ Error: {e}")
        return 1

if __name__ == "__main__":
    exit(main())