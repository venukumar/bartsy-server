package bartsy

class Notifications {

    static constraints = {
		order(nullable:true)
		orderType(nullable:true)
    }
	
	static belongsTo = [user:UserProfile,venue:Venue,order:Orders]
	
	String type
	String message
	String orderType
	Date dateCreated
	Date lastUpdated
}
