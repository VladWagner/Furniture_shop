import * as utils from "../../../infrastrucutre/utils";

export class UserDTO {
    constructor({id, name, email, role, createdAt, updatedAt, login, isConfirmed, profilePhoto, customerId}) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userLogin = login;
        this.isConfirmed = isConfirmed;
        this.profilePhoto = profilePhoto;
        this.customerId = customerId;
    }

    static readFromResponseData(resp) {
        return new UserDTO({
            id: resp.id,
            name: resp.name,
            email: resp.email,
            role: userRole.readFromResponseData(resp.role),
            login: resp.user_login,
            isConfirmed: resp.is_confirmed,
            profilePhoto: utils.getCorrectStaticFilePath(resp.profile_photo),
            customerId: resp?.customer_id,
            createdAt: resp.createdAt,
            updatedAt: resp.updatedAt
        });
    }
}

export class userRole {
    constructor({id, roleName}) {
        this.id = id;
        this.roleName = roleName;
    }

    static readFromResponseData(role) {
        return new userRole({
            id: role.id,
            roleName: role.user_role
        });
    }

}
