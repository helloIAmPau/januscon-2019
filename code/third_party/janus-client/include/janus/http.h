#pragma once

#include <curl/curl.h>
#include <memory>
#include <string>
#include <mutex>
#include <utility>
#include <unordered_map>

namespace Janus {

  using Headers = std::unordered_map<std::string, std::string>;
  using Query = std::unordered_map<std::string, std::string>;

  struct Response final {
    int32_t status;
    std::string body;

    Response(int32_t status, std::string body) : status(std::move(status)), body(std::move(body)) {}
  };

  struct Request final {
    std::string body;
    Headers headers;
    Query query;

    Request(std::string body, Query query, Headers headers) : body(std::move(body)), query(std::move(query)), headers(std::move(headers)) {}
    Request(std::string body, Query query) : Request(body, query, Headers()) {}
    explicit Request(Query query) : Request("", query, Headers()) {}
    explicit Request(std::string body) : Request(body, Query(), Headers()) {}
  };


  class Http {
   public:
     virtual Response get(std::string path) = 0;
     virtual Response get(std::string path, const Request& request) = 0;
     virtual Response post(std::string path, const Request& request) = 0;
     virtual Response post(std::string path) = 0;
     virtual void interrupt() = 0;
  };

  class HttpFactory {
   public:
     virtual std::shared_ptr<Http> create(const std::string& baseUrl) = 0;
  };

  class HttpFactoryImpl : public HttpFactory {
   public:
     std::shared_ptr<Http> create(const std::string& baseUrl);
  };

  enum HttpStatus { READY, CLOSE };

  class HttpImpl : public Http {
   public:
     explicit HttpImpl(const std::string& baseUrl);
     ~HttpImpl();

     Response get(std::string path);
     Response get(std::string path, const Request& request);
     Response post(std::string path, const Request& request);
     Response post(std::string path);

     void interrupt();
   private:
     HttpStatus _statusValue = HttpStatus::READY;
     std::mutex _statusMutex;
     HttpStatus _status();
     void _status(HttpStatus status);

     Response _request(const std::string& path, const std::string& method, const Request& request);

     static size_t _writeFunction(void *ptr, size_t size, size_t nmemb, std::string* data);
     static int _checkFunction(void* arg, curl_off_t dltotal, curl_off_t dlnow, curl_off_t ultotal, curl_off_t ulnow);

     std::string _baseUrl;
  };

}  // namespace Janus
