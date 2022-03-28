<?php

/*
|--------------------------------------------------------------------------
| Web Routes
|--------------------------------------------------------------------------
|
| Here is where you can register web routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| contains the "web" middleware group. Now create something great!
|
*/

use App\Http\Middleware\Client;
use Illuminate\Support\Facades\Route;

//USERCONTROLLER
Route::get('/', [
    "as" => "welcome", "uses" => "UserController@LoginShow"
]);

Route::post('/login', [
    "as" => "login", "uses" => 'UserController@Login'
]);

Route::get('/logout', [
    "as" => "logout", "uses" => 'UserController@Logout'
])->middleware(Client::class);

Route::post('/adduser', [
    "as" => "adduser", "uses" => 'UserController@AddUser'
])->middleware(Client::class);

Route::get('/userlist', [
    "as" => "userlist", "uses" => 'UserController@ShowUserList'
])->middleware(Client::class);

Route::get('/edituser/{id}', [
    "as" => "showedituser", "uses" => 'UserController@ShowEditUser'
])->middleware(Client::class);

Route::post('/edituser/', [
    "as" => "edituser", "uses" => 'UserController@EditUser'
])->middleware(Client::class);

Route::get('/deleteuser/{id}', [
    "as" => "deleteuser", "uses" => 'UserController@DeleteUser'
])->middleware(Client::class);

Route::get('/dumpuserexists', [
    "as" => "dumpuserexists", "uses" => 'UserController@DumpUserExists'
])->middleware(Client::class);


//LISTCONTROLLER
Route::get('/list/{type}', [
    "as" => "list", "uses" => 'ListController@ShowList'
])->middleware(Client::class);

Route::get('/reimportall/', [
    "as" => "reimportall", "uses" => 'ListController@ReimportAllData'
])->middleware(Client::class);

Route::get('/deletelist/{id}', [
    "as" => "deletelist", "uses" => 'ListController@DeleteListItem'
])->middleware(Client::class);

Route::post('/editlist/', [
    "as" => "editlist", "uses" => 'ListController@EditListItem'
])->middleware(Client::class);

Route::get('/editlist/{id}', [
    "as" => "showeditlist", "uses" => 'ListController@ShowEditListItem'
])->middleware(Client::class);

//JSONCONTROLLER
Route::get('/filelist', [
    "as" => "filelist", "uses" => 'JsonController@ShowFileList'
])->middleware(Client::class);

Route::post('/uploadfile', [
    "as" => "uploadfile", "uses" => 'JsonController@UploadFile'
])->middleware(Client::class);

Route::get('/deletefile/{id}', [
    "as" => "deletefile", "uses" => 'JsonController@DeleteFile'
])->middleware(Client::class);

Route::post('/editfile/', [
    "as" => "editfile", "uses" => 'JsonController@EditFile'
])->middleware(Client::class);

Route::get('/editfile/{id}', [
    "as" => "showeditfile", "uses" => 'JsonController@ShowEditFile'
])->middleware(Client::class);

//MAPCONTROLLER
Route::get('/maplist', [
    "as" => "maplist", "uses" => 'MapController@ShowMapList'
])->middleware(Client::class);

Route::post('/uploadmap', [
    "as" => "uploadmap", "uses" => 'MapController@UploadMap'
])->middleware(Client::class);

Route::post('/updatemap', [
    "as" => "updatemap", "uses" => 'MapController@UpdateMap'
])->middleware(Client::class);

Route::get('/updatemap/{id}', [
    "as" => "showupdatemap", "uses" => 'MapController@ShowUpdateMap'
])->middleware(Client::class);

Route::get('/deletemap/{id}', [
    "as" => "deletemap", "uses" => 'MapController@DeleteMap'
])->middleware(Client::class);

Route::post('/editmap/', [
    "as" => "editmap", "uses" => 'MapController@EditMap'
])->middleware(Client::class);

Route::get('/editmap/{id}', [
    "as" => "showeditmap", "uses" => 'MapController@ShowEditMap'
])->middleware(Client::class);

//POLYCONTROLLER
Route::get('/polylist', [
    "as" => "polylist", "uses" => 'PolyController@ShowPolyList'
])->middleware(Client::class);

Route::post('/uploadPoly', [
    "as" => "uploadPoly", "uses" => 'PolyController@UploadPoly'
])->middleware(Client::class);

Route::get('/deletePoly/{id}', [
    "as" => "deletePoly", "uses" => 'PolyController@DeletePoly'
])->middleware(Client::class);

Route::post('/editPoly/', [
    "as" => "editPoly", "uses" => 'PolyController@EditPoly'
])->middleware(Client::class);

Route::get('/editPoly/{id}', [
    "as" => "showeditPoly", "uses" => 'PolyController@ShowEditPoly'
])->middleware(Client::class);

//ICONCONTROLLER
Route::get('/iconlist', [
    "as" => "iconlist", "uses" => 'IconController@ShowIconList'
])->middleware(Client::class);

Route::post('/uploadicon', [
    "as" => "uploadicon", "uses" => 'IconController@UploadIcon'
])->middleware(Client::class);

Route::get('/deleteicon/{id}', [
    "as" => "deleteicon", "uses" => 'IconController@DeleteIcon'
])->middleware(Client::class);

Route::post('/editicon/', [
    "as" => "editicon", "uses" => 'IconController@EditIcon'
])->middleware(Client::class);

Route::get('/editicon/{id}', [
    "as" => "showediticon", "uses" => 'IconController@ShowEditIcon'
])->middleware(Client::class);

//CSVCONTROLLER
Route::get('/csvlist', [
    "as" => "csvlist", "uses" => 'CsvController@ShowCSVList'
])->middleware(Client::class);

Route::post('/uploadcsv', [
    "as" => "uploadcsv", "uses" => 'CsvController@UploadCSV'
])->middleware(Client::class);

Route::get('/deletecsv/{id}', [
    "as" => "deletecsv", "uses" => 'CsvController@DeleteCSV'
])->middleware(Client::class);

Route::post('/editcsv/', [
    "as" => "editcsv", "uses" => 'CsvController@EditCSV'
])->middleware(Client::class);

Route::get('/editcsv/{id}', [
    "as" => "showeditcsv", "uses" => 'CsvController@ShowEditCSV'
])->middleware(Client::class);

Route::get('/dumpnotfound', [
    "as" => "dumpnotfound", "uses" => 'CsvController@DumpNotFound'
])->middleware(Client::class);

//ATTRIBUTECONTROLLER
Route::get('/attributelist', [
    "as" => "attributelist", "uses" => 'AttributeController@ShowAttributeList'
])->middleware(Client::class);

Route::get('/deleteattribute/{id}', [
    "as" => "deleteattribute", "uses" => 'AttributeController@DeleteAttribute'
])->middleware(Client::class);

Route::post('/editattribute/', [
    "as" => "editattribute", "uses" => 'AttributeController@EditAttribute'
])->middleware(Client::class);

Route::get('/editattribute/{id}', [
    "as" => "showeditattribute", "uses" => 'AttributeController@ShowEditAttribute'
])->middleware(Client::class);

//CITYCONTROLLER
Route::get('/citylist', [
    "as" => "citylist", "uses" => 'CityController@ShowCityList'
])->middleware(Client::class);

Route::post('/parsecities', [
    "as" => "parsecities", "uses" => 'CityController@ParseCities'
])->middleware(Client::class);

Route::get('/deletecity/{id}', [
    "as" => "deletecity", "uses" => 'CityController@DeleteCity'
])->middleware(Client::class);

Route::post('/editcity/', [
    "as" => "editcity", "uses" => 'CityController@EditCity'
])->middleware(Client::class);

Route::get('/editcity/{id}', [
    "as" => "showeditcity", "uses" => 'CityController@ShowEditCity'
])->middleware(Client::class);

//LEGENDCONTROLLER
Route::get('/legendlist', [
    "as" => "legendlist", "uses" => 'LegendController@ShowLegendList'
])->middleware(Client::class);

Route::post('/insertlegend', [
    "as" => "insertlegend", "uses" => 'LegendController@InsertLegend'
])->middleware(Client::class);

Route::get('/deletelegend/{id}', [
    "as" => "deletelegend", "uses" => 'LegendController@DeleteLegend'
])->middleware(Client::class);

Route::post('/editlegend/', [
    "as" => "editlegend", "uses" => 'LegendController@EditLegend'
])->middleware(Client::class);

Route::get('/editlegend/{id}', [
    "as" => "showeditlegend", "uses" => 'LegendController@ShowEditLegend'
])->middleware(Client::class);
