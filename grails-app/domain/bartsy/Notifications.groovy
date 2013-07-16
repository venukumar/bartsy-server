package bartsy

class Notifications {

    static constraints = {
		order(nullable:true)
		orderType(nullable:true)
    }
	static mapping={
		status defaultValue: 0
	}
	
	static belongsTo = [user:UserProfile,venue:Venue,order:Orders]
	
	String type
	String message
	String orderType
	Date dateCreated
	Date lastUpdated
	int status
}
