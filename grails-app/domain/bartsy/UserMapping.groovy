package bartsy

class UserMapping {

    static constraints = {
    }
    
    static belongsTo = [userProfile:UserProfile,role:Role,bartsyUserDetails:BartsyUserDetails]
    String status
    
}
