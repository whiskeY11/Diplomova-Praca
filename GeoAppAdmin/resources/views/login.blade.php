<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
	<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css" integrity="sha384-9aIt2nRpC12Uk9gS9baDl411NQApFmC26EwAOH8WgZl5MYYxFfc+NcPb1dKGj7Sk" crossorigin="anonymous">
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>
	<script src="https://kit.fontawesome.com/a076d05399.js"></script>
	<link href="/docs/4.4/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous">
	<title>GeoApp</title>
    <link rel="stylesheet" type="text/css" href="{{asset('css/login.css?v=').time()}}">
</head>
<body class="text-center">
	<form class="form-signin" action="{{action('UserController@Login')}}" method="post">
		<div class="lineup">
			<h1 class="h3 mb-3 font-weight-normal">Prihlásenie do systému</h1>
			<div class="ln2 text-ln">
				<input type="text" id="email" name="email" class="form-control text-ln" placeholder="Email" required autofocus>
				<input type="password" id="password" name="password" class="form-control text-ln" placeholder="Heslo" required>
				<button class="btn btn-lg btn-block btn-sign" id="Submit" type="Submit">Prihlásiť</button>
                <input type="hidden" name="_token" value="{{csrf_token()}}">
			</div>
		</div>
        @if($error == 1)
		<div class="alert alert-danger">
            <div style="text-align: center">Skúste znova</div>
        </div>
        @elseif($error == 2)
        <div class="alert alert-danger">
            <div style="text-align: center">Nie ste admin!</div>
        </div>
        @else
            <div class="alert alert-danger collapse">
            </div>
        @endif
	</form>
</body>
</html>
