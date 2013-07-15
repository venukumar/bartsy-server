package bartsy

class UserProfile {

     static constraints = {
		 emailVerified(nullable:true)
		 sessionCode(nullable:true)
		 encryptedCreditCard(nullable:true)
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
		String bartsyPassword
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
		String googleId
		String googleUserName
		String email
		String facebookId
		String bartsyId
		String facebookUserName
		String gender
		int deviceType
		String deviceToken
		Date dateCreated
		Date lastUpdated
		String showProfile
		Date showProfileUpdated
		String bartsyLogin
		String creditCardNumber
		String expMonth
		String expYear
		String emailVerified
		String sessionCode
		String encryptedCreditCard
      }
