@extends("layouts.app")
@section("content")
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
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
        </div>
        <table class="table table-hover">
            <thead>
            <tr>
                <th scope="col">Názov entity</th>
                <th scope="col">Mesto</th>
                <th scope="col">Atribút</th>
                <th scope="col">Hodnota</th>
                <th scope="col">Možnosti</th>
            </tr>
            </thead>
            <tbody>
            @foreach ($list as $row)
                <tr>
                    <th scope="row">{{$row->list_name}}</th>
                    <td>{{$row->city_id == -1 ? "Neurčené" : $row->city_name}}</td>
                    <td>{{$row->csv_name}}</td>
                    <td>{{$row->value}}</td>
                    <td>
                        <a href="{{action('AttributeController@ShowEditAttribute', ["id" => $row->id])}}"><i class="fas fa-edit" style="color:black"></i></a> |
                        <a href="{{action('AttributeController@DeleteAttribute', ["id" => $row->id])}}" onclick="return confirm('Atribúta bude vymazaná.');"><i class="fas fa-trash alt" style="color:black"></i></a>
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
