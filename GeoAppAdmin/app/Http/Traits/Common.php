<?php

namespace App\Http\Traits;

use GuzzleHttp\Client;
use Illuminate\Support\Facades\Config;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Redirect;
use Illuminate\Support\Facades\Session;

trait Common
{
    private function GetTimeInMillis() {
        return round(microtime(true) * 1000);
    }

    private function APIPath() {
        return Config::get("data.url_prefix").Config::get("data.server_path");
    }

    private function APIStoragePath() {
        return Config::get("data.url_prefix").Config::get("data.storage_path");
    }

    private function GrantType() {
        return Config::get("data.grant_type");
    }

    private function ClientID() {
        return Config::get("data.client_id");
    }

    private function ClientSecret() {
        return Config::get("data.client_secret");
    }

    private function MakeRequest($URI, $data, $useOAuth) {
        $client = new Client(); # ['http_errors' => false]
        $header = array('Content-Type' => 'application/x-www-form-urlencoded', 'Accept' => 'application/json');
        if($useOAuth) {
            $header["Authorization"] = "Bearer ".Session::get("token");
        }
        $params['headers'] = $header;
        $params['form_params'] = $data;
        try {
            $response = $client->post($URI, $params);
            if ($response->getStatusCode() == 200) {
                return json_decode($response->getBody(), true);
            } else {
                return null;
            }
        } catch (\GuzzleHttp\Exception\RequestException $ex) {
            return $ex->getResponse()->getBody()->getContents();
        }
    }

    private function MakeMultipartRequest($URI, $multipart, $useOAuth) {
        $client = new Client(); # ['http_errors' => false]
        $header = array('Content-Type' => 'application/x-www-form-urlencoded', 'Accept' => 'application/json');
        if($useOAuth) {
            $header["Authorization"] = "Bearer ".Session::get("token");
        }
        $params['headers'] = $header;
        $params['multipart'] = $multipart;
        $response = $client->post($URI, $params);
        if($response->getStatusCode() == 200) {
            return json_decode($response->getBody(), true);
        } else {
            return null;
        }
    }

    private function Compress($data) {
        $compressed = gzdeflate($data,  9);
        $compressed = gzdeflate($compressed,  9);
        $final = base64_encode($compressed);
        return $final;
    }

    private function ToHome() {
        return Redirect::to("/");
    }
}
