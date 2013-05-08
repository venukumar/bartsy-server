package bartsy

class UserProfile {

     static constraints = {
         
    }
	
	static mapping = {
		 //userImage(sqlType:'blob')
	}
        
   // static belongsTo = [bartsyUserDetails:BartsyUserDetails]
	
        String name
        //String middleName
		//String lastName
	//String location
	//String emailAddress
	//String mobileNumber
	//String preference
        //String gender
        //String relationshipStatus
        //String userStatus
       // String language1
        //String language2
        //byte[] userImage
		String loginId
		long bartsyId
		String loginType
		String userName
		String gender
		int deviceType
		String deviceToken
      }
