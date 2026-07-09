Add-Type -AssemblyName System.Drawing
$bmp1 = New-Object System.Drawing.Bitmap(64, 64)
$g1 = [System.Drawing.Graphics]::FromImage($bmp1)
$g1.Clear([System.Drawing.Color]::Black)
$bmp1.Save("src\main\resources\assets\legendmagic\textures\entity\dark_wolf\dark_wolf.png")

$bmp2 = New-Object System.Drawing.Bitmap(16, 16)
$g2 = [System.Drawing.Graphics]::FromImage($bmp2)
$g2.Clear([System.Drawing.Color]::DarkOrchid)
$bmp2.Save("src\main\resources\assets\legendmagic\textures\item\ring_of_the_dark_emperor.png")
