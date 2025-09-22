-- Add cccd_image_url column to users table
-- This column will store the file path of the uploaded CCCD image

ALTER TABLE users
ADD COLUMN cccd_image_url VARCHAR(255) NULL
COMMENT 'File path to the uploaded CCCD (Citizen ID) image';

-- Optional: Add index for better query performance if needed
-- CREATE INDEX idx_users_cccd_image_url ON users(cccd_image_url);