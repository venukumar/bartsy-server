package bartsy

class UserProfile {

     static constraints = {
         
    }
	
	static mapping = {
		// userImage(sqlType:'blob')
	}
        
   // static belongsTo = [bartsyUserDetails:BartsyUserDetails]
	
        String name
        String firstName
		String lastName
		String dateOfBirth
		String description
		String orientation
		String status
		String nickName
		String password
	//String location
	//String emailAddress
	//String mobileNumber
	//String preference
        //String gender
        //String relationshipStatus
        //String userStatus
       // String language1
        //String language2
        String userImage
		String loginId
		long bartsyId
		String loginType
		String userName
		String gender
		int deviceType
		String deviceToken
		Date dateCreated
		Date lastUpdated
		String showProfile
		Date showProfileUpdated
		String emailId
		String creditCardNumber
		String expMonth
		String expYear	
      }
