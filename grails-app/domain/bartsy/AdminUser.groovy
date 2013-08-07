package bartsy

class AdminUser {

    static constraints = {
		firstName(nullable:true)
		lastName(nullable:true)
		email(nullable:true)
		username()
		password()
		userType(inList: ["Admin","SalesUser","SalesManager","Promoter"])
		dateCreated()
		lastUpdated()
		promoterCode(nullable:true)
		status()
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
	int status
	
	static mapping={
		version false
	}
}
