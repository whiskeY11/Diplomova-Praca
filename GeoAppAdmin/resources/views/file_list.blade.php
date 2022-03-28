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

<!-- AddModal -->
<div class="modal fade" id="addModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title" id="exampleModalLabel">Pridať JSON</h4>
            </div>
            <form method="post" action="{{action('JsonController@UploadFile')}}" enctype="multipart/form-data">
                <div class="modal-body">
                    <div class="row">
                        <div class="col-xs-12 col-sm-12 col-md-12">
                            <div class="form-group">
                                <label for="file"><b>Súbor</b></label>
                                <input type="file" accept=".json,.geojson" class="form-control-file" name="file" required autofocus><br/>
                                <label for="icon"><b>Ikona</b></label><br/>

                                <div class="row">
                                    <div class="col-6">
                                        <select onchange="changeImage(this)" class="form-control" id="icon" name="icon">
                                            @foreach($icons as $row)
                                                <option value="{{$row->id}}">{{$row->name}}</option>
                                            @endforeach
                                        </select>
                                    </div>
                                    <div class="col">
                                        <img id="iconPreview" src="{{$iconsName["defaultIcon"]}}" class="responsive">
                                    </div>
                                </div>
                                <br/>
                                <label for="city"><b>Mesto</b></label><br/>
                                <div class="row">
                                    <div class="col-6">
                                        <select class="form-control" id="city" name="city">
                                            @foreach($cities as $row)
                                                <option value="{{$row->id}}">{{$row->name}}</option>
                                            @endforeach
                                        </select>
                                    </div>
                                </div>
                                <br/> <br/>

                                <div class="form-check-inline">
                                    <label class="form-check-label">
                                        <input type="checkbox" class="form-check-input" value="" name="load">Použiť dáta (v databáze)
                                    </label>
                                </div>

                                <input type="hidden" name="_token" value="{{csrf_token()}}">
                            </div>
                        </div>
                    </div>

                </div>
                <div class="modal-footer">
                    <input class="btn btn-primary" type="submit" name="submit" value="Pridať">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Zavrieť</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div class="col-sm-9">
    <div class="well">
        <div class="table-title">
            <h4>Zoznam</h4>
            <div>
                <a class="btn btn-outline-primary" data-toggle="modal" data-target="#addModal" role="button" style="text-decoration: none">Pridať</a>
            </div>
        </div>
        <table class="table table-hover">
            <thead>
            <tr>
                <th scope="col">Meno</th>
                <th scope="col">Dáta použité v databáze</th>
                <th scope="col">Vytvorený z Poly súboru</th>
                <th scope="col">Mesto</th>
                <th scope="col">Ikona</th>
                <th scope="col">Možnosti</th>
            </tr>
            </thead>
            <tbody>
            @foreach ($list as $row)
                <tr>
                    <th scope="row">{{$row->name}}</th>
                    <td>{{$row->loaded == 1 ? "Áno" : "Nie"}}</td>
                    <td>{{$row->poly_id == -1 ? "Žiadny - Pridané z GeoJSONu" : $row->poly_id}}</td>
                    <td>{{$row->city_id == -1 ? "Neurčené" : $row->city_id}}</td>
                    <td>{{$row->icon}} <img src="{{$iconsName[$row->icon]}}" alt="{{$row->icon}}" class="responsiveSmall"></td>
                    <td>
                        <a href="{{action('JsonController@ShowEditFile', ["id" => $row->id])}}"><i class="fas fa-edit" style="color:black"></i></a> |
                        <a href="{{action('JsonController@DeleteFile', ["id" => $row->id])}}" onclick="return confirm('Súbor, aj všetky pridané dáta z tohto súboru, budú vymazané.');"><i class="fas fa-trash alt" style="color:black"></i></a>
                    </td>
                </tr>
            @endforeach
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
<script>
    var $iconsId= @json($iconsId);

    function changeImage(el) {
        document.getElementById("iconPreview").src = $iconsId[el.value];
    }
</script>
@endsection
