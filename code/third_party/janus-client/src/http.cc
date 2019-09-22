#include "janus/http.h"
#include <cstring>
#include "janus/json.h"

namespace Janus {

  HttpImpl::HttpImpl(const std::string& baseUrl) {
    curl_global_init(CURL_GLOBAL_ALL);
    this->_baseUrl = baseUrl;
  }

  HttpImpl::~HttpImpl() {
    curl_global_cleanup();
  }

  Response HttpImpl::get(std::string path) {
    return this->_request(path, "GET", Request("", Query(), Headers()));
  }

  Response HttpImpl::get(std::string path, const Request& request) {
    return this->_request(path, "GET", request);
  }

  Response HttpImpl::post(std::string path, const Request& request) {
    return this->_request(path, "POST", request);
  }

  Response HttpImpl::post(std::string path) {
    return this->_request(path, "POST", Request("", Query(), Headers()));
  }

  void HttpImpl::interrupt() {
    this->_status(HttpStatus::CLOSE);
  }

  Response HttpImpl::_request(const std::string& path, const std::string& method, const Request& request) {
    auto handle = curl_easy_init();

    curl_easy_setopt(handle, CURLOPT_SSL_VERIFYPEER, false);
    curl_easy_setopt(handle, CURLOPT_SSL_VERIFYHOST, false);
    curl_easy_setopt(handle, CURLOPT_USERAGENT, "Janus Native HTTP Client");

    auto index = 0;
    std::string query = "";
    for (auto pair : request.query) {
      auto prefix = index++ == 0 ? "?" : "&";
      query += (prefix + pair.first + "=" + pair.second);
    }

    auto fullUrl = this->_baseUrl + path + query;
    curl_easy_setopt(handle, CURLOPT_URL, fullUrl.c_str());
    curl_easy_setopt(handle, CURLOPT_CUSTOMREQUEST, method.c_str());

    curl_easy_setopt(handle, CURLOPT_POSTFIELDS, request.body.c_str());
    curl_easy_setopt(handle, CURLOPT_POSTFIELDSIZE, std::strlen(request.body.c_str()));

    std::string bodyString = "";
    curl_easy_setopt(handle, CURLOPT_WRITEFUNCTION, HttpImpl::_writeFunction);
    curl_easy_setopt(handle, CURLOPT_WRITEDATA, &bodyString);

    curl_easy_setopt(handle, CURLOPT_XFERINFOFUNCTION, HttpImpl::_checkFunction);
    curl_easy_setopt(handle, CURLOPT_XFERINFODATA, this);
    curl_easy_setopt(handle, CURLOPT_NOPROGRESS, false);

    struct curl_slist* headers = curl_slist_append(nullptr, "Content-Type: application/json");
    curl_easy_setopt(handle, CURLOPT_HTTPHEADER, headers);

    long status = curl_easy_perform(handle);
    if (status == CURLE_OK) {
      curl_easy_getinfo(handle, CURLINFO_RESPONSE_CODE, &status);
    } else {
      auto reply = std::make_shared<JSON>();
      reply->string("janus", "error");
      reply->string("transaction", "__internal__");
      auto error = std::make_shared<JSON>();
      error->integer("code", status);
      error->string("reason", "http error " + std::to_string(status));
      reply->object("error", error);

      bodyString = reply->serialize();
    }

    curl_slist_free_all(headers);
    curl_easy_cleanup(handle);

    return Response(status, bodyString);
  }

  HttpStatus HttpImpl::_status() {
    std::lock_guard<std::mutex> lock(this->_statusMutex);
    return this->_statusValue;
  }

  void HttpImpl::_status(HttpStatus status) {
    std::lock_guard<std::mutex> lock(this->_statusMutex);
    this->_statusValue = status;
  }

  size_t HttpImpl::_writeFunction(void* ptr, size_t size, size_t nmemb, std::string* data) {
    data->append(reinterpret_cast<char*>(ptr), size * nmemb);
    return size * nmemb;
  }

  int HttpImpl::_checkFunction(void* arg, curl_off_t dltotal, curl_off_t dlnow, curl_off_t ultotal, curl_off_t ulnow) {
    auto context = reinterpret_cast<HttpImpl*>(arg);

    return context->_status();
  }

  std::shared_ptr<Http> HttpFactoryImpl::create(const std::string& baseUrl) {
    auto http = std::make_shared<HttpImpl>(baseUrl);

    return http;
  }

}  // namespace Janus
