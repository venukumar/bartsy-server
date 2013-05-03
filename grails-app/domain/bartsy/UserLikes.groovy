package bartsy

class UserLikes {

    static constraints = {
    }
    
    static belongsTo = [user:UserProfile,likesuser:UserProfile]
}
