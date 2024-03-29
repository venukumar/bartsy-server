package bartsy

import java.util.Date;

class Cocktails {

	static constraints = {
		cocktailId(nullable:true)
		name(nullable:true)
		category(nullable:true)
		alcohol(nullable:true)
		glass(nullable:true)
		ingredients(nullable:true)
		instructions(nullable:true)
		price(nullable:true)
		available(nullable:true)
		description(nullable:true)
		shopping(nullable:true)
	}
	static belongsTo =[venue:Venue]
	static mapping = {
		instructions type:'text'
		ingredients type:'text'
	}
	long cocktailId
	String name
	String category
	String alcohol
	String glass
	String ingredients
	String instructions
	float price
	String available
	Date dateCreated
	Date lastUpdated
	String description
	String shopping
}
