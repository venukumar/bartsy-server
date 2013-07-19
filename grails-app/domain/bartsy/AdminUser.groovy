package bartsy

class AdminUser {

    static constraints = {
		firstName(nullable:true)
		lastName(nullable:true)
		email(nullable:true)
		username()
		password()
		userType(inList: ["Admin","SalesUser","SalesManager"])
		dateCreated()
		lastUpdated()
		promoterCode(nullable:true)
    }
	String firstName
	String lastName
	String email
	String username
	String password
	String userType
	Date dateCreated
	Date lastUpdated
	String promoterCode
	
	static mapping={
		version false
	}
}
