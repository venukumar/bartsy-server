package bartsy

import java.text.SimpleDateFormat

class CommonMethods {

	/**
	 * 
	 * To calculate the age based on DOB
	 * 
	 * @param dob
	 * @return
	 */
	def getAge(String dob){
		// Date is in YYYY-MM-DD format.
		int yearDOB = Integer.parseInt(dob.substring(0, 4));
		int monthDOB = Integer.parseInt(dob.substring(5, 7)); 
		int dayDOB = Integer.parseInt(dob.substring(8, 10)); 
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy"); 
		Date date = new Date(); 
		int thisYear = Integer.parseInt(dateFormat.format(date)); 
		dateFormat = new SimpleDateFormat("MM"); 
		date = new java.util.Date(); 
		int thisMonth = Integer.parseInt(dateFormat.format(date)); 
		dateFormat = new SimpleDateFormat("dd"); 
		date = new java.util.Date();
		 int thisDay = Integer.parseInt(dateFormat.format(date));
		  int age = thisYear-yearDOB; 
		  if(thisMonth < monthDOB){ 
			  age = age-1;
			   }
		   if(thisMonth == monthDOB && thisDay < dayDOB){ 
			   age = age-1; 
			   } 
		   System.out.println("The age of user is : " + age);
		   return age
		   
	}

}
