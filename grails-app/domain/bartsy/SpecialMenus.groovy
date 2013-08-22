package bartsy

class SpecialMenus {

	static hasMany=[specialMenuItems : SpecialMenuItems]
	static belongsTo =[venue:Venue]
	static constraints = { menuName(nullable:false) }
	String menuName
	static mapping = {
		specialMenuItems cascade: 'all-delete-orphan' 
	}
}
