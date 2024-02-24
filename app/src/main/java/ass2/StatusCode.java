package ass2;

enum StatusCode {
    OK(200, "OK"),
    REDIRECT(302, "Redirect"),
    UNAUTHORIZED(401, "Unauthorized"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    public int statusCode;
    public String message;

    StatusCode(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public String toString() {
        return this.statusCode + " " + this.message;
    }
}