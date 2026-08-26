-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: internship
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `banktransfer_table`
--
-- WHERE:  bankCode IS NULL

LOCK TABLES `banktransfer_table` WRITE;
/*!40000 ALTER TABLE `banktransfer_table` DISABLE KEYS */;
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (1,NULL,'ながれぼし銀行',NULL,NULL,NULL,'0',NULL,NULL,NULL);
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (2,NULL,'ながれぼし銀行',NULL,NULL,NULL,'1234567',NULL,NULL,NULL);
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (3,NULL,'ながれぼし銀行',NULL,NULL,NULL,'0',NULL,NULL,NULL);
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (4,NULL,'銀行',NULL,NULL,NULL,'1234567',NULL,NULL,NULL);
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (5,NULL,'銀行',NULL,NULL,NULL,'1234567',NULL,NULL,NULL);
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (6,NULL,'山陰共同銀行',NULL,NULL,NULL,'1234567',NULL,NULL,NULL);
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (7,NULL,'山陰共同銀行',NULL,'本店','普通','1234567','タナカ タロウ',NULL,NULL);
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (8,NULL,'山陰共同銀行',NULL,'本店','普通','1234567','田中一郎',100000,'2026-10-22 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (9,NULL,'',NULL,'','','','',NULL,NULL);
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (10,NULL,'',NULL,'','','','',NULL,NULL);
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (11,NULL,'ながれぼし銀行',NULL,'本店','当座','1234567','あああ',14234234,'2000-10-22 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (12,NULL,'そらいろ銀行',NULL,'本店','普通','1234567','あああ',12334567,'2003-10-22 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (13,NULL,'ながれぼし銀行',NULL,'本店','普通','1234567','adfsadf',12345,'2333-11-22 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (14,NULL,'ながれぼし銀行',NULL,'本店','普通','1234567','aaabbb',1234,'1234-12-31 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (15,NULL,'ながれぼし銀行',NULL,'本店','普通','1234567','田中一郎',1234567,'2222-11-22 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (16,NULL,'かぜまち銀行',NULL,'箕面半町店','普通','1234567','Java',12345678,'1111-11-11 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (17,NULL,'ながれぼし銀行',NULL,'小野原東店','普通','1234567','aaa',1234567,'1111-11-11 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (18,NULL,'ながれぼし銀行',NULL,'吹田店','普通','1234567','aaabbb',12344,'1111-11-11 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (19,NULL,'ながれぼし銀行',NULL,'吹田店','普通','1234567','aaabbb',12344,'1111-11-11 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (20,NULL,'こもれび銀行',NULL,'北千里店','普通','1234567','aaabbb',12345,'1111-11-11 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (21,NULL,'つきのわ銀行',NULL,'箕面半町店','普通','1234567','田中一郎',1234567,'2003-10-22 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (22,NULL,'つきのわ銀行',NULL,'箕面半町店','普通','1234567','田中一郎',1234567,'2003-10-22 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (23,NULL,'ながれぼし銀行',NULL,'箕面半町店','普通','1234567','田中一郎',123456,'2009-08-08 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (24,NULL,'ながれぼし銀行',NULL,'箕面半町店','普通','1234567','ﾔﾏﾀﾞ ﾀﾛｳ',123456,'2026-08-27 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (25,NULL,'ながれぼし銀行',NULL,'本店','普通','1234567','ﾔﾏﾀﾞ ﾀﾛｳ',1234,'2026-08-26 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (26,NULL,'つきのわ銀行',NULL,'箕面半町店','普通','1234567','ｱ',1234567,'2026-08-25 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (27,NULL,'そらいろ銀行',NULL,'箕面半町店','普通','1234567','ｱ',12345,'2026-08-25 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (28,NULL,'ながれぼし銀行',NULL,'本店','普通','1234567','ｱ',123456,'2026-08-25 00:00:00');
INSERT INTO `banktransfer_table` (`id`, `bankCode`, `bankName`, `branchCode`, `branchName`, `bankAccountType`, `bankAccountNum`, `name`, `money`, `transferDateTime`) VALUES (29,NULL,'ながれぼし銀行',NULL,'本店','当座','1234567','ｱ',1234567,'2026-08-26 00:00:00');
/*!40000 ALTER TABLE `banktransfer_table` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-26 10:48:15
