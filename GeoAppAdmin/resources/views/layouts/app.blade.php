<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
	<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css" integrity="sha384-9aIt2nRpC12Uk9gS9baDl411NQApFmC26EwAOH8WgZl5MYYxFfc+NcPb1dKGj7Sk" crossorigin="anonymous">
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>
	<script src="https://kit.fontawesome.com/a076d05399.js"></script>
	<title>GeoApp</title>
    <link rel="stylesheet" type="text/css" href="{{asset('css/app.css')}}">
</head>
<body>
<nav id="navbar" class="navbar navbar-dark fixed-top" style="background-color: #0062ff;">
    <a class="navbar-brand"> GeoApp </a>
    <a class="btn btn-outline-secondary logout-btn" href="{{action('UserController@Logout')}}" role="button">Odhlásiť</a>
</nav>
<div class="container-fluid" style="margin-top:3%">
    <div class="row content">
        <ul class="nav flex-column bgc-nav col-lg-2 col-md-3 sidebar">
            <li><a href="{{action('ListController@ShowList', ["type" => "all"])}}">
                    <div style={{session("current") == "all" ? "font-weight:bold" : ""}}>Databáza</div></a>
                <ul style="list-style-type:none;">
                    <li><a href="{{action('ListController@ShowList', ["type" => "Point"])}}">
                            <div style={{session("current") == "Point" ? "font-weight:bold" : ""}}>Body</div></a></li>
                    <li><a href="{{action('ListController@ShowList', ["type" => "LineString"])}}">
                            <div style={{session("current") == "LineString" ? "font-weight:bold" : ""}}>Ulice</div></a></li>
                    <li><a href="{{action('ListController@ShowList', ["type" => "Polygon"])}}">
                            <div style={{session("current") == "Polygon" ? "font-weight:bold" : ""}}>Oblasti</div></a></li>
                    <li><a href="{{action('AttributeController@ShowAttributeList')}}">
                            <div style={{session("current") == "attribute" ? "font-weight:bold" : ""}}>Atribúty</div></a></li>
                    <li><a href="{{action('LegendController@ShowLegendList')}}">
                            <div style={{session("current") == "legend" ? "font-weight:bold" : ""}}>Legendy</div></a></li>
                </ul>
            </li>
            <li><a href="{{action('JsonController@ShowFileList')}}">
                    <div style={{session("current") == "file" ? "font-weight:bold" : ""}}>Súbory</div></a>
                <ul style="list-style-type:none;">
                    <li><a href="{{action('JsonController@ShowFileList')}}">
                            <div style={{session("current") == "file" ? "font-weight:bold" : ""}}>JSON</div></a></li>
                    <li><a href="{{action('CsvController@ShowCSVList')}}">
                            <div style={{session("current") == "csv" ? "font-weight:bold" : ""}}>CSV (Atribúty)</div></a></li>

                    <li><a href="{{action('IconController@ShowIconList')}}">
                            <div style={{session("current") == "icon" ? "font-weight:bold" : ""}}>Ikony</div></a></li>
                </ul>
            </li>
            <li><a href="{{action('MapController@ShowMapList')}}">
                    <div style={{session("current") == "map" ? "font-weight:bold" : ""}}>Mapové údaje</div></a>
                <ul style="list-style-type:none;">
                    <li><a href="{{action('MapController@ShowMapList')}}">
                            <div style={{session("current") == "map" ? "font-weight:bold" : ""}}>Mapy (PBF)</div></a></li>
                    <li><a href="{{action('CityController@ShowCityList')}}">
                            <div style={{session("current") == "city" ? "font-weight:bold" : ""}}>Mestá</div></a></li>
                    <li><a href="{{action('PolyController@ShowPolyList')}}">
                            <div style={{session("current") == "poly" ? "font-weight:bold" : ""}}>Poly (ulice)</div></a></li>
                </ul>
            <li><a href="{{action('UserController@ShowUserList')}}">
                    <div style={{session("current") == "user" ? "font-weight:bold" : ""}}>Používatelia</div></a></li>
        </ul>
        <br>
        <br>
        @yield('content')
    </div>
</div>
</body>
