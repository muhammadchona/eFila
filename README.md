iDART
======
iDART is a software solution designed to support the dispensing of ARV drugs in the public health care sector. It supports pharmacists in their important role of dispensing accurately to an increasing number of patients whilst still being able to engage and assist the patient.

iDART is a product of [Cell-Life](http://www.cell-life.org/).
This branch is a fork of the code to be able to manage general purpose pharmacies instead of only ARV clinics.

The main project source is hosted at [iDART-Sourceforge](http://sourceforge.net/projects/idart/) and the Wiki for the same is located at [iDART - Cell-Life](http://wiki.cell-life.org/display/IDART)

What is iDART?
--
iDART is a software solution designed to support the dispensing of ARV drugs in the public health care sector. It supports pharmacists in their important role of dispensing accurately to an increasing number of patients whilst still being able to engage and assist the patient.
The intelligent Dispensing of ART software is used by the pharmacist to manage the supplies of ARV stocks, print reports and manage collection of drugs by patients. The software is also designed to address the reporting requirements of Government, international funders (such as PEPFAR) and internal clinical data such as identifying patients who are have not collected their medication for an extended period of time.

Why does it exist?
--
* to increase capacity of pharmacies in public health clinics by providing a software that is focused on the specific needs of ARV dispensing.
* to facilitate dispensing in remote public health clinics
* to support the so-called "down - referral process"
* to support clinic information management by providing information on the status quo of patients.

Interoperability with OpenMRS
--
In this version of Idart FGH has developed it's interoperability with OpenMRS. All dispenses of ARV drugs made through iDART will be reflected in OpenMRS with the creation of the FILA form.

FGH has used REST Web Service module to develop iDART - OpenMRS interoperability.

Prerequisites to run iDART - OpenMRS interoperability
--
1. OpenMRS instance running
2. Load [Rest Web Services](https://modules.openmrs.org/#/show/153/webservices-rest) into OpenMRS instance. For this scenario version 2.9.39bd19 was used.
3. Run this script against iDART database: ALTER TABLE regimeterapeutico ADD COLUMN regimenomeespecificado character varying(60);  
