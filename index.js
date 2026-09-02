export default {
    async fetch(request) {
        const url = new URL(request.url);
        const targetHost = "posbackend-production-4e83.up.railway.app";

        // Change the destination URL
        url.hostname = targetHost;

        // Create a new headers object and overwrite the Host header
        const newHeaders = new Headers(request.headers);
        newHeaders.set("Host", targetHost);
        newHeaders.set("Origin", "https://" + targetHost);

        // Prepare the new request
        const init = {
            method: request.method,
            headers: newHeaders,
            redirect: "follow"
        };

        // Only attach the body if it is not a GET or HEAD request
        if (request.method !== "GET" && request.method !== "HEAD") {
            init.body = request.body;
        }

        // Forward the request to Railway
        return fetch(url.toString(), init);
    }
};