package bartsy

class Messages {

    static mapping = {
		message type:'text'
    }
	
	static belongsTo = [sender:UserProfile,receiver:UserProfile,venue:Venue]
	
	String message
	Date dateCreated
	Date lastUpdated
}
