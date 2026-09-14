db = db.getSiblingDB("erp_db");

db.createUser({
    user: "erp_db_user",
    pwd: "secretL0calPassword",
    roles: [
        {
            role: "readWrite",
            db: "erp_db"
        }
    ]
});
