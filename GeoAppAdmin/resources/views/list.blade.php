@extends("layouts.app")
@section("content")
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
    <link rel="stylesheet" type="text/css" href="{{asset('css/app.css?v=').time()}}">
    <link rel="stylesheet" type="text/css" href="{{asset('css/list.css?v=').time()}}">
</head>
<body>
<div class="col-sm-9">
    <div class="well">
        <div class="table-title">
            <h4>Zoznam</h4>
            <div>
                <a class="btn btn-outline-primary" href="{{action('ListController@ReimportAllData')}}" role="button" style="text-decoration: none" onclick="return confirm('Všetky údaje v datábaze budú vymazané a nanovo pridané.');">Re-import</a>
            </div>
        </div>
        <table class="table table-hover">
            <thead>
            <tr>
                <th scope="col">Meno</th>
                <th scope="col">Typ</th>
                <th scope="col">Mesto</th>
                <th scope="col">Ikona</th>
                <th scope="col">Pridané používateľom</th>
                <th scope="col">Pridaný zo súboru</th>
                <th scope="col">Možnosti</th>
            </tr>
            </thead>
            <tbody>
            @foreach ($list as $row)
                <tr>
                    <th scope="row">{{$row->name}}</th>
                    <td>{{$row->type}}</td>
                    <td>{{$row->city_id == -1 ? "Neurčené" : $row->city_id}}</td>
                    @if($row->type=="Point")
                        <td>{{$row->icon}} <img src="{{$iconsArray[$row->icon]}}" alt="{{$row->icon}}" class="responsiveSmall"></td>
                    @else
                        <td>-</td>
                    @endif
                    <td>{{$row->user_id == -1 ? "Administrátor" : $row->user_id}}</td>
                    <td>{{$row->loadjson_id == -1 ? "Žiadny - Pridané používateľom" : $row->loadjson_id}}</td>
                    <td>
                        <a href="{{action('ListController@ShowEditListItem', ["id" => $row->id])}}"><i class="fas fa-edit" style="color:black"></i></a> |
                        <a href="{{action('ListController@DeleteListItem', ["id" => $row->id])}}" onclick="return confirm('Položka bude vymazaná.');"><i class="fas fa-trash alt" style="color:black"></i></a>
                    </td>
                </tr>
            @endforeach
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
@endsection
