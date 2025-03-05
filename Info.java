public class Info {
	
	// From oracle Document 1618482.1
	// Note: Oracle's "java -version" prints out the system properties named java.version (first line), java.runtime.version (2nd line), java.vm.name, java.vm.version, and java.vm.info (3rd line).
	
	// use -XshowSettings:properties -version
  
  private static final String PROPS[] = {
    "java.vendor",
    "java.version",
//    "java.vendor.version", // necessary to see GraalVM or not
    "java.runtime.version",
    "java.vm.name",
    "java.runtime.name"
  };
  
  private static final String INFO_VERSION = "1.9";
  private static final String COMPILER = "JDK1.2.2";
  private static final String FIELD_SEPARATOR = ";";

  private static void displayProps() {
    int nbprops = PROPS.length;
    System.out.print("Columns: ");
    for(int i = 0; i<nbprops; i++) {
      if(i != 0)
        System.out.print(FIELD_SEPARATOR);
      System.out.print(PROPS[i]);
    }
    System.out.print(FIELD_SEPARATOR);
    System.out.print("olicense");
    System.out.print(FIELD_SEPARATOR);
    System.out.print("ocost");
    System.out.println();
  }
  
  public static void main(String[] args) {
    if(args.length >= 1) {
      System.out.println("Syntax: java Info [-?]");
      System.out.println("Version: " + INFO_VERSION);
      System.out.println("Compiler: " + COMPILER);
      if("-?".equals(args[0]))
        displayProps();
      System.exit(0);
    }
    int nbprops = PROPS.length;
    for(int i = 0; i<nbprops; i++) {
      if(i != 0)
        System.out.print(FIELD_SEPARATOR);
      System.out.print("\"" + System.getProperty(PROPS[i]) + "\"");
    }
    // add now license and cost
    Info info = new Info();
    try {
	    info.setJavaPropsLowerCase();
	    info.analyseLicenseAndCost();
    }
    catch(Exception e) {
    	// safety, it should never happen...
    	info.setLicenseAndCostError();
    }
    System.out.print(FIELD_SEPARATOR);
    System.out.print("\"" + info.getLicense() + "\"");
    System.out.print(FIELD_SEPARATOR);
    System.out.print("\"" + info.getCost() + "\"");
    System.out.println();
  }
  
  /**
   * --------------------------------------------------------------------------
   * License analyzer
   * Some versions such as 11.0.21.0.1 have been found !
   * Version is vA.vB.vC.vD
   */

  private String javaVendorLowerCase = null;
  private String javaVendorVersionLowerCase = null;
  private String javaVersionLowerCase = null;
  private String javaVMNameLowerCase = null;

  private String license = null; // TBD, BCLA, NTFC, etc.
  private String cost = null; // TBD, Freeware, Commercial
  
  public static final String TBD = "TBD";
  public static final String NON_ORACLE = "Non Oracle";
  public static final String ERROR = "error";

  public static final String LICENSE_BCLA = "BCLA";
  public static final String LICENSE_OTN = "OTN";
  public static final String LICENSE_GOTN = "GOTN";
  public static final String LICENSE_NFTC = "NFTC";
  public static final String LICENSE_GFTC = "GFTC";
  public static final String LICENSE_NFTC_TBC = "?NFTC?";
  public static final String LICENSE_GFTC_TBC = "?GFTC?";
  public static final String LICENSE_OPENJDK = "OpenJDK";
  public static final String LICENSE_GRAAL_EE = "Graal EE";
  
  public static final String COST_FREEWARE = "Freeware";
  public static final String COST_FREEWARE_TBC = "?Freeware?";
  public static final String COST_COMMERCIAL = "Commercial";
  
  public static final String GRAAL_VM_LOWERCASE = "graal";
  public static final String GRAAL_EE_VM_LOWERCASE = "graalvm ee";

  public static final String VENDOR_ORACLE_LOWERCASE = "oracle";
  public static final String VENDOR_SUNMICROSYSTEMS_LOWERCASE = "sun microsystems";
  public static final String VENDOR_REDHAT_LOWERCASE = "redhat";
  public static final String VMNAME_OPENJDK_LOWERCASE = "openjdk";

  private int vA = -1;
  private int vB = -1;
  private int vC = -1;
  private int vD = -1;
  
  public Info() {
  }
  
  public String getLicense() {
	  return license;
  }
  
  public String getCost() {
	  return cost;
  }
  
  /**
   * 
   * @return parsed version as vA.vB.vC.vD . When a value is undefined, it is set to -1
   */
  public String getVAVBVCVD () {
	  return vA + "." + vB + "." + vC + "." + vD;
  }
  
  public void setJavaPropsLowerCase() {
	  javaVendorLowerCase = System.getProperty("java.vendor");
	  javaVendorVersionLowerCase = System.getProperty("java.vendor.version");
	  javaVersionLowerCase = System.getProperty("java.version");
	  javaVMNameLowerCase = System.getProperty("java.vm.name");
	  cleanupJavaPropsToLowerCase();
  }
  
  private void cleanupJavaPropsToLowerCase() {
	  if(javaVendorLowerCase == null)
		  javaVendorLowerCase = "";
	  if(javaVendorVersionLowerCase == null)
		  javaVendorVersionLowerCase = "";
	  if(javaVersionLowerCase == null)
		  javaVersionLowerCase = "";
	  if(javaVMNameLowerCase == null)
		  javaVMNameLowerCase = "";
	  javaVendorLowerCase = javaVendorLowerCase.trim().toLowerCase();
	  javaVendorVersionLowerCase = javaVendorVersionLowerCase.trim().toLowerCase();
	  javaVersionLowerCase = javaVersionLowerCase.trim().toLowerCase();
	  javaVMNameLowerCase = javaVMNameLowerCase.trim().toLowerCase();
  }
  
  public void setJavaPropsLowerCaseForTestPurpose(String aJavaVendor, String aJavaVersion, String aJavaVendorVersion, String aJavaVMName) {
	  javaVendorLowerCase = aJavaVendor;
	  javaVersionLowerCase = aJavaVersion;
	  javaVendorVersionLowerCase = aJavaVendorVersion;
	  javaVMNameLowerCase = aJavaVMName;
	  cleanupJavaPropsToLowerCase();
  }
  
  private boolean isOracleOrSunVendor() {
	  //handle the strange case of RedHat version
	  if (javaVersionLowerCase.indexOf(VENDOR_REDHAT_LOWERCASE) != -1)
		  return false;
	  if (javaVendorLowerCase.indexOf(VENDOR_SUNMICROSYSTEMS_LOWERCASE) != -1)
		  return true;
	  if (javaVendorLowerCase.indexOf(VENDOR_ORACLE_LOWERCASE) != -1)
		  return true;
	  return false;
  }
  
  private boolean isOpenJDK() {
	 return javaVMNameLowerCase.indexOf(VMNAME_OPENJDK_LOWERCASE) != -1;
  }
  
  private boolean isGraalVM() {
	 return javaVendorVersionLowerCase.indexOf(GRAAL_VM_LOWERCASE) != -1;
  }
  
  private boolean isGraalEEVM() {
	 return javaVendorVersionLowerCase.indexOf(GRAAL_EE_VM_LOWERCASE) != -1;
  }
  
  private void setLicenseAndCostBCLA() {
	  license = LICENSE_BCLA;
	  cost = COST_FREEWARE;
  }
  
  private void setLicenseAndCostOTN() {
	  license = LICENSE_OTN;
	  cost = COST_COMMERCIAL;
  }

  private void setLicenseAndCostGOTN() {
	  license = LICENSE_GOTN;
	  cost = COST_COMMERCIAL;
  }

  private void setLicenseAndCostNFTC() {
	  license = LICENSE_NFTC;
	  cost = COST_FREEWARE;
  }

  private void setLicenseAndCostGFTC() {
	  license = LICENSE_GFTC;
	  cost = COST_FREEWARE;
  }

  private void setLicenseAndCostNFTC_TBC() {
	  license = LICENSE_NFTC_TBC;
	  cost = COST_FREEWARE_TBC;
  }

  private void setLicenseAndCostGFTC_TBC() {
	  license = LICENSE_GFTC_TBC;
	  cost = COST_FREEWARE;
  }

  private void setLicenseAndCostOpenJDK() {
	  license = LICENSE_OPENJDK;
	  cost = COST_FREEWARE;
  }
  
  private void setLicenseAndCostTBD() {
	  license = TBD;
	  cost = TBD;
  }

  private void setLicenseAndCostNonOracle() {
	  license = NON_ORACLE;
	  cost = NON_ORACLE;
  }

  public void setLicenseAndCostError() {
	  license = ERROR;
	  cost = ERROR;
  }
  
  private void setLicenseAndCostGraalEE() {
	  license = LICENSE_GRAAL_EE;
	  cost = COST_COMMERCIAL;	  
  }

  /**
   * parse up to 4 numbers, with
   *   . as only separator between first 3 numbers
   *   .,_,- as only possible separator between 3rd and 4th number
   * stop after 2 consecutive separators
   * if first number is identified, by default others will be set to 0
   */
  int javaVersionIndex;
  int javaVersionLength;
  
  private int parseNextNumber(String allowedSeparators) {
	  String foundNumberAsString = "";
	  while(javaVersionIndex < javaVersionLength) {
		  char c = javaVersionLowerCase.charAt(javaVersionIndex);
		  if (Character.isDigit(c)) {
			  foundNumberAsString += c;
		  }
		  else {
			  if (allowedSeparators.indexOf(c) != -1) {
				  //found separator
				  javaVersionIndex++;
				  break;
			  }
			  //found not a digit, not a separator
			  javaVersionIndex = javaVersionLength;
		  }
		  javaVersionIndex++;
	  }
	  int foundNumber = -1;
	  try {
		  foundNumber = Integer.parseInt(foundNumberAsString, 10);
	  }
	  catch(NumberFormatException e) {
		  foundNumber = -1;
	  }
	  return foundNumber;
  }
  
  private void parseJavaVersion() {
	  javaVersionIndex = 0;
	  javaVersionLength = javaVersionLowerCase.length();
	  int nextNumber = 0;
	  //vA
	  nextNumber = parseNextNumber(".");
	  if(nextNumber == -1)
		  return;
	  vA = nextNumber;
	  vB = vC = vD = 0;
	  //vB
	  nextNumber = parseNextNumber(".");
	  if(nextNumber == -1)
		  return;
	  vB = nextNumber;
	  //vC
	  nextNumber = parseNextNumber(".-_");
	  if(nextNumber == -1)
		  return;
	  vC = nextNumber;
	  //vD
	  nextNumber = parseNextNumber("");
	  if(nextNumber == -1)
		  return;
	  vD = nextNumber;
  }
  
  public void parseJavaVersionForTest() {
	  parseJavaVersion();
  }
  
  private void analyseJavaVersionVA1() {
	  if(vB == -1) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  switch (vB) {
	  case 1:
		  analyseJavaVersionVA1VB1();
		  break;
	  case 2:
		  analyseJavaVersionVA1VB2();
		  break;
	  case 3:
		  analyseJavaVersionVA1VB3();
		  break;
	  case 4:
		  analyseJavaVersionVA1VB4();
		  break;
	  case 5:
		  analyseJavaVersionVA1VB5();
		  break;
	  case 6:
		  analyseJavaVersionVA1VB6();
		  break;
	  case 7:
		  analyseJavaVersionVA1VB7();
		  break;
	  case 8:
		  analyseJavaVersionVA1VB8();
		  break;
	  default:
		  setLicenseAndCostTBD();
		  break;
	  }
  }
  
  private void analyseJavaVersionVA1VB1() {
	  // https://www.oracle.com/java/technologies/java-archive-downloads-javase11-downloads.html
	  // max BCLA version 1.1.1
	  // max BCLA version 1.1.3
	  // max BCLA version 1.1.7B_007
	  // max BCLA version 1.1.8_16
	  // no info for the rest
	  if(vC == -1 || vD == -1) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if(vC == 0 || vC == 2 || vC == 4 || vC == 5 || vC == 6) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if ( (vC == 1 && vD > 0) ||
		   (vC == 3 && vD > 0) ||
		   (vC == 7 && vD > 7) ||
		   (vC == 8 && vD > 16) ||
		   (vC > 8) ) {
		  setLicenseAndCostOTN();
		  return;
	  }
	  setLicenseAndCostBCLA();
  }
  
  private void analyseJavaVersionVA1VB2() {
	  // https://www.oracle.com/java/technologies/java-archive-javase-v12-downloads.html
	  // no info for version 1.2.0
	  // max BCLA version 1.2.1_04
	  // max BCLA version 1.2.2_17
	  if(vC == -1 || vD == -1) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if(vC == 0) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if( (vC == 1 && vD > 4) ||
		  (vC == 2 && vD > 17) ||
		  (vC > 2) ) {
		  setLicenseAndCostOTN();
		  return;
	  }
	  setLicenseAndCostBCLA();
  }
  
  private void analyseJavaVersionVA1VB3() {
	  // https://www.oracle.com/java/technologies/java-archive-javase-v13-downloads.html
	  // max BCLA version 1.3.0_05
	  // max BCLA version 1.3.1_29
	  if(vC == -1 || vD == -1) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if( (vC == 0 && vD > 5) ||
		  (vC == 1 && vD > 29) ||
		  (vC > 1) ) {
		  setLicenseAndCostOTN();
		  return;
	  }
	  setLicenseAndCostBCLA();
  }
  
  private void analyseJavaVersionVA1VB4() {
	  // https://www.oracle.com/java/technologies/java-archive-javase-v14-downloads.html
	  // max BCLA version 1.4.0_04
	  // max BCLA version 1.4.1_07
	  // max BCLA version 1.4.2_30
	  if( (vC == -1 || vD == -1) ||
          (vC == 0 && vD > 4) ||
          (vC == 1 && vD > 7) ||
          (vC == 2 && vD > 30) ||
          (vC > 2) ) {
		  setLicenseAndCostOTN();
		  return;
	  }
	  setLicenseAndCostBCLA();
  }
  
  private void analyseJavaVersionVA1VB5() {
	  // https://www.oracle.com/java/technologies/java-archive-javase5-downloads.html
	  // max BCLA version 1.5.0_22
	  if(vC == -1 || vD == -1) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if( (vC == 0 && vD > 22) ||
		  (vC > 0) ){
		  setLicenseAndCostOTN();
		  return;
	  }
	  setLicenseAndCostBCLA();
  }
  
  private void analyseJavaVersionVA1VB6() {
	  // https://www.oracle.com/java/technologies/javase-java-archive-javase6-downloads.html
	  // max BCLA version 1.6.0_45
	  if(vC == -1 || vD == -1) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if( (vC == 0 && vD > 45) ||
		  (vC > 0) ) {
		  setLicenseAndCostOTN();
		  return;
	  }
	  setLicenseAndCostBCLA();
  }
  
  private void analyseJavaVersionVA1VB7() {
	  // https://www.oracle.com/java/technologies/javase/javase7-archive-downloads.html
	  // max BCLA version 1.7.0_80
	  if(vC == -1 || vD == -1) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if( (vC == 0 && vD > 80) ||
		  (vC > 0) ) {
		  setLicenseAndCostOTN();
		  return;
	  }
	  setLicenseAndCostBCLA();
  }
  
  private void analyseJavaVersionVA1VB8() {
	  // LTS
	  // https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html
	  // https://www.oracle.com/java/technologies/javase/javase8u211-later-archive-downloads.html
	  // max BCLA version 1.8.0_202
	  if(vC == -1 || vD == -1) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if( (vC == 0 && vD > 202) ||
		  (vC > 0) ) {
		  setLicenseAndCostOTN();
		  return;
	  }
	  setLicenseAndCostBCLA();	  
  }
  
  private void analyseJavaVersionVA9() {
	  // https://www.oracle.com/java/technologies/javase/javase9-archive-downloads.html
	  // version 9.0.2 and 9.0.3 can not be downloaded
	  // max BCLA version 9.0.4
	  if(vB == -1 || vC == -1) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if(vB > 0) {
		  setLicenseAndCostOTN();
		  return;
	  }
	  if(vB == 2 || vB == 3 || vC > 4) {
		  setLicenseAndCostOTN();
		  return;
	  }
	  if(vD > 0) {
		  setLicenseAndCostOTN();
		  return;
	  }
	  setLicenseAndCostBCLA();	  
  }
  
  private void analyseJavaVersionVA10() {
	  // https://www.oracle.com/java/technologies/java-archive-javase10-downloads.html
	  // max BCLA version 10.0.2
	  if(vB == -1 || vC == -1) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if(vB == 0 && vC > 2) {
		  setLicenseAndCostOTN();
		  return;
	  }
	  if(vB > 0) {
		  setLicenseAndCostOTN();
		  return;
	  }
	  if(vD > 0) {
		  setLicenseAndCostOTN();
		  return;
	  }
	  setLicenseAndCostBCLA();	  
  }
  
  private void analyseJavaVersionVA11to16() {
	  // LTS for vA=11
	  setLicenseAndCostOTN();
  }
  
  private void analyseJavaVersionVA17to23() {
	  if(vB > 0) {
		  setLicenseAndCostOTN();
		  return;
	  }
	  if(vB == -1 || vC == -1) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if(vA == 17) {
		  // LTS
		  // https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
		  // max NFTC version 17.0.3.1
		  // max NFTC version 17.0.4.1
		  // max NFTC version 17.0.12
		  if (vC == 3 || vC == 4) {
			  if (vD > 1)
				  setLicenseAndCostOTN();
			  else
				  setLicenseAndCostNFTC();
			  return;
		  }
		  if(vC > 12 || vD > 0)
			  setLicenseAndCostOTN();
		  else
			  setLicenseAndCostNFTC();
		  return;
	  }
	  if(vA == 18) {
		  // https://www.oracle.com/java/technologies/javase/jdk18-archive-downloads.html
		  // max NFTC version 18.0.0.0
		  // max NFTC version 18.0.1.1
		  // max NFTC version 18.0.2.1
		  if( (vC == 0 && vD > 0) ||
			  (vC == 1 && vD > 1) ||
			  (vC == 2 && vD > 1) ||
			  (vC > 2) ) {
			  setLicenseAndCostOTN();
			  return;
		  }
		  setLicenseAndCostNFTC();
		  return;
	  }
	  if(vA == 19) {
		  // https://www.oracle.com/java/technologies/javase/jdk19-archive-downloads.html
		  // max NFTC version 19.0.2
		  if(vC > 2 || vD > 0)
			  setLicenseAndCostOTN();
		  else
			  setLicenseAndCostNFTC();
		  return;
	  }
	  if(vA == 20) {
		  // https://www.oracle.com/java/technologies/javase/jdk20-archive-downloads.html
		  // max NFTC version 20.0.2
		  if(vC > 2 || vD > 0)
			  setLicenseAndCostOTN();
		  else
			  setLicenseAndCostNFTC();
		  return;
	  }
	  if(vA == 21) {
		  // LTS
		  // https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html
		  // https://www.oracle.com/java/technologies/downloads/#java21
		  // max NFTC version is probably 21.0.12 (as of Dec. 2024)
		  if(vC > 12 || vD > 0)
			  setLicenseAndCostOTN(); //@TODO To recheck after Sep. 2026
		  else
			  setLicenseAndCostNFTC();
		  return;
	  }
	  if(vA == 22) {
		  // https://www.oracle.com/java/technologies/javase/jdk22-archive-downloads.html
		  // max NFTC version 22.0.2
		  if(vC > 2 || vD > 0)
			  setLicenseAndCostOTN();
		  else
			  setLicenseAndCostNFTC();
		  return;
	  }
	  if(vA == 23) {
		  // https://www.oracle.com/java/technologies/downloads/#java23
		  // max NFTC version 23.0.2 (as of Feb. 2025)
		  if(vC > 2 || vD > 0)
			  setLicenseAndCostNFTC_TBC(); //@TODO To recheck after Mar. 2025
		  else
			  setLicenseAndCostNFTC();
		  return;
	  }
	  setLicenseAndCostTBD();
  }
  
  /**
   * Normal Java case, not Graal
   */
  private void analyseJavaVersion() {
	  switch (vA) {
	  case 1:
		  analyseJavaVersionVA1();
		  break;
	  case 9:
		  analyseJavaVersionVA9();
		  break;
	  case 10:
		  analyseJavaVersionVA10();
		  break;
	  case 11:
	  case 12:
	  case 13:
	  case 14:
	  case 15:
	  case 16:
		  analyseJavaVersionVA11to16();
		  break;
	  case 17:
	  case 18:
	  case 19:
	  case 20:
	  case 21:
	  case 22:
	  case 23:
		  analyseJavaVersionVA17to23();
		  break;
	  default:
		  setLicenseAndCostTBD();
		  break;
	  }
  }
  
  /**
   * Graal Java case
   */
  private void analyseGraalJavaVersion() {
	  if(isGraalEEVM()) {
		  setLicenseAndCostGraalEE();
		  return;
	  }
	  if(vA < 17 || vA > 23) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if(vB == -1 || vC == -1) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if(vB > 0) {
		  setLicenseAndCostGOTN();
		  return;
	  }
	  if(vA == 17) {
		  // LTS
		  // https://www.oracle.com/java/technologies/javase/graalvm-jdk17-archive-downloads.html
		  // GFTC version 17.0.7 to 12
		  if (vC >= 7 && vC <= 12 && vD == 0) {
			  setLicenseAndCostGFTC();
			  return;
		  }
		  setLicenseAndCostGOTN();
		  return;
	  }
	  if(vA == 18 || vA == 19) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if(vA == 20) {
		  // https://www.oracle.com/java/technologies/javase/graalvm-jdk20-archive-downloads.html
		  // GFTC version 20.0.1 to 2
		  if (vC >= 1 && vC <= 2 && vD == 0) {
			  setLicenseAndCostGFTC();
			  return;
		  }
		  setLicenseAndCostGOTN();
		  return;
	  }
	  if(vA == 21) {
		  // LTS
		  // https://www.oracle.com/java/technologies/downloads/#graalvmjava21
		  // https://www.oracle.com/java/technologies/javase/graalvm-jdk21-archive-downloads.html
		  // GFTC version 21, 21.0.1 to 6, probably up to 12
		  if (vC >= 0 && vC <= 12 && vD == 0) {
			  setLicenseAndCostGFTC();
			  return;
		  }
		  setLicenseAndCostGOTN();
		  return;
	  }
	  if(vA == 22) {
		  // https://www.oracle.com/java/technologies/javase/graalvm-jdk22-archive-downloads.html
		  // GFTC version 22, 21.0.1 to 2
		  if (vC >= 0 && vC <= 2 && vD == 0) {
			  setLicenseAndCostGFTC();
			  return;
		  }
		  setLicenseAndCostGOTN();
		  return;
	  }
	  if(vA == 23) {
		  // https://www.oracle.com/java/technologies/downloads/#graalvmjava23
		  // https://www.oracle.com/java/technologies/javase/graalvm-jdk23-archive-downloads.html
		  // GFTC version 23, 23.0.1 to 2 (as of Feb. 2025)
		  if(vC > 2 || vD > 0)
			  setLicenseAndCostGFTC_TBC(); //@TODO To recheck after Mar. 2025
		  else
			  setLicenseAndCostGFTC();
		  return;
	  }
	  setLicenseAndCostTBD();
  }
  
  public void analyseLicenseAndCost() {
	  //ensure first the vendor is defined
	  if(javaVendorLowerCase.length() == 0) {
		  setLicenseAndCostTBD();
		  return;
	  }
	  if (!isOracleOrSunVendor()) {
		  setLicenseAndCostNonOracle();
		  return;
	  }
	  if(isOpenJDK()) {
		  setLicenseAndCostOpenJDK();
		  return;
	  }
	  parseJavaVersion();
	  if(isGraalVM())
		  analyseGraalJavaVersion();
	  else
		  analyseJavaVersion();
  }
  
}
