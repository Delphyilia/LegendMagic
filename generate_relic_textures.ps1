Add-Type -AssemblyName System.Drawing

# 1. relic_shard.png
$bmp = New-Object System.Drawing.Bitmap(16, 16)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.Clear([System.Drawing.Color]::Transparent)
$b1 = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 148, 0, 211))
$b2 = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 218, 112, 214))
$pts = @(
    (New-Object System.Drawing.Point(8, 1)),
    (New-Object System.Drawing.Point(14, 8)),
    (New-Object System.Drawing.Point(8, 14)),
    (New-Object System.Drawing.Point(2, 8))
)
$g.FillPolygon($b1, $pts)
$ptsInner = @(
    (New-Object System.Drawing.Point(8, 4)),
    (New-Object System.Drawing.Point(12, 8)),
    (New-Object System.Drawing.Point(8, 12)),
    (New-Object System.Drawing.Point(4, 8))
)
$g.FillPolygon($b2, $ptsInner)
$bmp.Save("src\main\resources\assets\legendmagic\textures\item\relic_shard.png", [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()

# 2. iino_doue.png
$bmp2 = New-Object System.Drawing.Bitmap(16, 16)
$g2 = [System.Drawing.Graphics]::FromImage($bmp2)
$g2.Clear([System.Drawing.Color]::Transparent)
$bgBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 30, 0, 0))
$g2.FillEllipse($bgBrush, 1, 1, 14, 14)
$redBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 220, 20, 60))
$g2.FillRectangle($redBrush, 7, 3, 2, 10)
$g2.FillRectangle($redBrush, 3, 7, 10, 2)
$centerBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 255, 215, 0))
$g2.FillRectangle($centerBrush, 7, 7, 2, 2)
$bmp2.Save("src\main\resources\assets\legendmagic\textures\item\iino_doue.png", [System.Drawing.Imaging.ImageFormat]::Png)
$bmp2.Dispose()

Write-Host "Textures generated successfully!"
