<?php

/*
|--------------------------------------------------------------------------
| Application Routes
|--------------------------------------------------------------------------
|
| Here is where you can register all of the routes for an application.
| It is a breeze. Simply tell Lumen the URIs it should respond to
| and give it the Closure to call when that URI is requested.
|
*/

$router->get('/', function () use ($router) {
    return $router->app->version();
});

//CITYCONTROLLER
$router->post('parseCities', [
    'middleware' => 'client',
    'as' => 'parseCities', 'uses' => 'CityController@ParseCities'
]);

$router->post('removeCity', [
    'middleware' => 'client',
    'as' => 'removeCity', 'uses' => 'CityController@RemoveCity'
]);

$router->post('editCity', [
    'middleware' => 'client',
    'as' => 'editCity', 'uses' => 'CityController@EditCity'
]);

//CSVCONTROLLER
$router->post('addCsv', [
    'middleware' => 'client',
    'as' => 'addCsv', 'uses' => 'CsvController@InsertCSV'
]);

$router->post('removeCsv', [
    'middleware' => 'client',
    'as' => 'removeCsv', 'uses' => 'CsvController@RemoveCSV'
]);

$router->post('editCsv', [
    'middleware' => 'client',
    'as' => 'editCsv', 'uses' => 'CsvController@EditCSV'
]);

$router->post('editAttribute', [
    'middleware' => 'client',
    'as' => 'editAttribute', 'uses' => 'CsvController@EditAttribute'
]);

$router->post('deleteAttribute', [
    'middleware' => 'client',
    'as' => 'deleteAttribute', 'uses' => 'CsvController@DeleteAttribute'
]);

$router->get('loadCsv/{id}', [
    'as' => 'loadCsv', 'uses' => 'CsvController@LoadDataFromCSV'
]);

//ICONCONTROLLER
$router->post('addIcon', [
    'middleware' => 'client',
    'as' => 'addIcon', 'uses' => 'IconController@InsertIcon'
]);

$router->post('removeIcon', [
    'middleware' => 'client',
    'as' => 'removeIcon', 'uses' => 'IconController@RemoveIcon'
]);

$router->post('editIcon', [
    'middleware' => 'client',
    'as' => 'editIcon', 'uses' => 'IconController@EditIcon'
]);

//MAPCONTROLLER
$router->post('addMap', [
    'middleware' => 'client',
    'as' => 'addMap', 'uses' => 'MapController@InsertMap'
]);

$router->post('removeMap', [
    'middleware' => 'client',
    'as' => 'removeMap', 'uses' => 'MapController@RemoveMap'
]);

$router->post('editMap', [
    'middleware' => 'client',
    'as' => 'editMap', 'uses' => 'MapController@EditMap'
]);

$router->post('addMapLegend', [
    'middleware' => 'client',
    'as' => 'addMapLegend', 'uses' => 'MapController@InsertMapLegend'
]);

$router->post('removeMapLegend', [
    'middleware' => 'client',
    'as' => 'removeMapLegend', 'uses' => 'MapController@RemoveMapLegend'
]);

$router->post('editMapLegend', [
    'middleware' => 'client',
    'as' => 'editMapLegend', 'uses' => 'MapController@EditMapLegend'
]);

//POLYCONTROLLER
$router->post('addPoly', [
    'middleware' => 'client',
    'as' => 'addPoly', 'uses' => 'PolyController@InsertPoly'
]);

$router->post('removePoly', [
    'middleware' => 'client',
    'as' => 'removePoly', 'uses' => 'PolyController@RemovePoly'
]);

$router->post('editPoly', [
    'middleware' => 'client',
    'as' => 'editPoly', 'uses' => 'PolyController@EditPoly'
]);

//DBCONTROLLER
$router->post('addFile', [
    'middleware' => 'client',
    'as' => 'addFile', 'uses' => 'DBController@addFile'
]);

$router->post('editFile', [
    'middleware' => 'client',
    'as' => 'editFile', 'uses' => 'DBController@editFile'
]);

$router->post('removeJson', [
    'middleware' => 'client',
    'as' => 'removeJson', 'uses' => 'DBController@removeJson'
]);

$router->post('reimportAll', [
    'middleware' => 'client',
    'as' => 'reimportAll', 'uses' => 'DBController@reimportAll'
]);

$router->get('addBounds', [
    'as' => 'addBounds', 'uses' => 'DBController@addBounds'
]);

$router->get('exportBounds', [
    'as' => 'exportBounds', 'uses' => 'DBController@exportBounds'
]);

//USERCONTROLLER
$router->post('registerUser', [
    'as' => 'registerUser', 'uses' => 'UserController@registerUser'
]);

$router->post('registerUserFromAdmin', [
    'middleware' => 'client',
    'as' => 'registerUserFromAdmin', 'uses' => 'UserController@registerUserFromAdmin'
]);

$router->post('editUser', [
    'middleware' => 'client',
    'as' => 'editUser', 'uses' => 'UserController@editUser'
]);

$router->post('checkUserParams', [
    'middleware' => 'client',
    'as' => 'checkUserParams', 'uses' => 'UserController@checkUserParams'
]);

$router->post('disableForceDownload', [
    'middleware' => 'client',
    'as' => 'disableForceDownload', 'uses' => 'UserController@disableForceDownload'
]);

$router->post('disableIconDownload', [
    'middleware' => 'client',
    'as' => 'disableIconDownload', 'uses' => 'UserController@disableIconDownload'
]);

$router->post('disableAttributeDownload', [
    'middleware' => 'client',
    'as' => 'disableAttributeDownload', 'uses' => 'UserController@disableAttributeDownload'
]);

$router->post('disableCityDownload', [
    'middleware' => 'client',
    'as' => 'disableCityDownload', 'uses' => 'UserController@disableCityDownload'
]);

$router->post('disableLegendDownload', [
    'middleware' => 'client',
    'as' => 'disableLegendDownload', 'uses' => 'UserController@disableLegendDownload'
]);

$router->post('synchronizeUser', [
    'middleware' => 'client',
    'as' => 'synchronizeUser', 'uses' => 'UserController@synchronizeUser'
]);

//APPCONTROLER
$router->post('addItemAndCords', [
    'middleware' => 'client',
    'as' => 'addItemAndCords', 'uses' => 'AppController@addItemAndCords'
]);

$router->post('editItem', [
    'middleware' => 'client',
    'as' => 'editItem', 'uses' => 'AppController@editItem'
]);

$router->post('editItemLocation', [
    'middleware' => 'client',
    'as' => 'editItemLocation', 'uses' => 'AppController@editItemLocation'
]);

$router->post('removeItemAndCords', [
    'middleware' => 'client',
    'as' => 'removeItemAndCords', 'uses' => 'AppController@removeItemAndCords'
]);

$router->post('getList', [
    'middleware' => 'client',
    'as' => 'getList', 'uses' => 'AppController@toGeoJSON'
]);

$router->get('getListGET/{type}', [
    'as' => 'getListGET', 'uses' => 'AppController@toGeoJSONGET'
]);

$router->post('getBounds', [
    'middleware' => 'client',
    'as' => 'getBounds', 'uses' => 'AppController@getBounds'
]);

$router->post('getIcons', [
    'middleware' => 'client',
    'as' => 'getIcons', 'uses' => 'AppController@getIcons'
]);

$router->post('getCities', [
    'middleware' => 'client',
    'as' => 'getCities', 'uses' => 'AppController@getCities'
]);

$router->post('getAttributes', [
    'middleware' => 'client',
    'as' => 'getAttributes', 'uses' => 'AppController@getAttributes'
]);


