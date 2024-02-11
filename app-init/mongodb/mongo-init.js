db.createUser(
    {
        user: "demo",
        pwd: "audio123!",
        roles: [
            { role: "readWrite", db: "songs" }
        ]
    })