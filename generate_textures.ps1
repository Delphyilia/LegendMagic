Add-Type -AssemblyName System.Drawing

# 1. dark_wolf
$bmp1 = New-Object System.Drawing.Bitmap(64, 64)
$g1 = [System.Drawing.Graphics]::FromImage($bmp1)
$g1.Clear([System.Drawing.Color]::Black)
$bmp1.Save("src\main\resources\assets\legendmagic\textures\entity\dark_wolf\dark_wolf.png")
$bmp1.Dispose()

# 2. ring_of_the_dark_emperor
$bmp2 = New-Object System.Drawing.Bitmap(16, 16)
$g2 = [System.Drawing.Graphics]::FromImage($bmp2)
$g2.Clear([System.Drawing.Color]::DarkOrchid)
$bmp2.Save("src\main\resources\assets\legendmagic\textures\item\ring_of_the_dark_emperor.png")
$bmp2.Dispose()

# 3. thunder_wolf
$dir = "src\main\resources\assets\legendmagic\textures\entity\thunder_wolf"
if (-not (Test-Path $dir)) {
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
}
$bmp3 = New-Object System.Drawing.Bitmap(64, 64)
$g3 = [System.Drawing.Graphics]::FromImage($bmp3)
$g3.Clear([System.Drawing.Color]::FromArgb(255, 20, 50, 110))
$pen = New-Object System.Drawing.Pen([System.Drawing.Color]::Cyan, 2)
$g3.DrawLine($pen, 12, 12, 52, 52)
$g3.DrawLine($pen, 52, 12, 12, 52)
$bmp3.Save("src\main\resources\assets\legendmagic\textures\entity\thunder_wolf\thunder_wolf.png", [System.Drawing.Imaging.ImageFormat]::Png)
$bmp3.Dispose()

# 4. ring_of_thunder_beasts
$bmp4 = New-Object System.Drawing.Bitmap(16, 16)
$g4 = [System.Drawing.Graphics]::FromImage($bmp4)
$g4.Clear([System.Drawing.Color]::Transparent)
$goldBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::Gold)
$g4.FillEllipse($goldBrush, 2, 2, 12, 12)
$cyanBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::Cyan)
$g4.FillEllipse($cyanBrush, 5, 5, 6, 6)
$bmp4.Save("src\main\resources\assets\legendmagic\textures\item\ring_of_thunder_beasts.png", [System.Drawing.Imaging.ImageFormat]::Png)
$bmp4.Dispose()
