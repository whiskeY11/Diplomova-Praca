<?php

namespace App\Http\Controllers;

use DateTime;
use DateTimeZone;
use Illuminate\Http\Request;
use Illuminate\Routing\Controller;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Redirect;
use Illuminate\Support\Facades\Session;
use App\Http\Traits\Common;

class UserController extends Controller
{
    use Common;

    private static $userExistsCode = 1;

    public function LoginShow() {
        if(Session::has("user_id")) {
            return Redirect::to("list/all");
        }

        return view("login", ["error" => 0]);
    }

    private function LoginSuccessful() {
        return Redirect::to("/list/all");
    }

    private function LoginFailed() {
        Session::flush();
        return view("login", ["error" => 1]);
    }

    private function LoginNotAdmin() {
        Session::flush();
        return view("login", ["error" => 2]);
    }

    private function IsAdmin() {
        return DB::table('users')->where('id', Session::get("user_id"))->first()->role_id == 2;
    }

    public function Logout() {
        Session::flush();
        return $this->ToHome();
    }

    public function Login(Request $request) {
        $params = array("email" => $request["email"], "grant_type" => $this->GrantType(), "client_id" => $this->ClientID(),
            "client_secret" => $this->ClientSecret(), "username" => $request["email"], "password" => $request["password"]);
        $response = $this->MakeRequest($this->APIPath()."oauth/token", $params, false);
        if($response != null) {
            if(isset($response["access_token"]))
            {
                Session::put('token', $response["access_token"]);
            }
            else
            {
                return $this->LoginFailed();
            }

            $response = $this->MakeRequest($this->APIPath()."checkUserParams", null, true);
            if($response != null) {
                Session::put("user_id", $response["user_id"]);
                if($this->IsAdmin()) {
                    return $this->LoginSuccessful();
                } else {
                    return $this->LoginNotAdmin();
                }
            } else {
                return $this->LoginFailed();
            }
        } else {
            return $this->LoginFailed();
        }
    }

    public function ShowUserList() {
        $list = DB::table("users")->get();

        foreach($list as $row) {
            if($row->last_login_time != 0) {
                $seconds = $row->last_login_time / 1000;
                $latest = new DateTime("@$seconds");
                $latest->setTimezone(new DateTimeZone("Europe/Bratislava"));
                $row->last_login_time = $latest->format('d.m.Y H:i:s');
            }
        }
        Session::put("current", "user");
        return view("user_list", compact("list"));
    }

    public function ShowEditUser($id) {
        $user = DB::table("users")->where("id", $id)->first();
        return view("user_edit", compact("user"));
    }

    public function AddUser(Request $request) {
        $params = array(
            "id" => $request["id"],
            "email" => $request["email"],
            "password" => $request["password"],
            "role_id" => $request["role_id"]
        );
        $response = $this->MakeRequest($this->APIPath()."registerUserFromAdmin", $params, true);
        $this->HandleUserExists($request["email"], $response);
        return Redirect::to("/userlist");
    }

    public function EditUser(Request $request) {
        $params = array(
            "id" => $request["id"],
            "email" => $request["email"],
            "password" => $request["password"] != null ? $request["password"] : "old",
            "role_id" => $request["role_id"],
            "force_download" => $request->has("force_download") ? 1 : 0,
            "icon_download" => $request->has("icon_download") ? 1 : 0,
            "attribute_download" => $request->has("attribute_download") ? 1 : 0,
            "city_download" => $request->has("city_download") ? 1 : 0,
            "legend_download" => $request->has("legend_download") ? 1 : 0
        );
        $response = $this->MakeRequest($this->APIPath()."editUser", $params, true);
        $this->HandleUserExists($request["email"], $response);
        return Redirect::to("/userlist");
    }

    public function DeleteUser($id) {
        DB::table("users")->where("id", $id)->delete();
        DB::table("list")->where("user_id", $id)->delete();
        return Redirect::to("/userlist");
    }

    private function HandleUserExists($name, $response)
    {
        if($response["result"] == self::$userExistsCode) {
            Session::put("userexists", $name);
        } else {
            Session::forget("userexists");
        }
    }

    public function DumpUserExists() {
        Session::forget("userexists");
        return Redirect::to("/userlist");
    }
}
