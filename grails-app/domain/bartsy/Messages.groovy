package bartsy

class Messages {

    static constraints = {
    }
	
	static belongsTo = [sender:UserProfile,receiver:UserProfile,venue:Venue]
	
	String message
	Date dateCreated
	Date lastUpdated
}
