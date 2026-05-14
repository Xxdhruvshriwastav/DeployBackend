$services = @('analytics-service', 'application-service', 'auth-service', 'interview-service', 'job-service', 'notification-service', 'payment-service', 'profile-service', 'subscription-service')
foreach ($svc in $services) {
  $file = "C:\Users\rudra\Desktop\job-portal\hireconnect\$svc\pom.xml"
  if (Test-Path $file) {
    $content = Get-Content $file -Raw
    if ($content -notmatch 'springdoc-openapi-starter-webmvc-ui') {
      $idx = $content.IndexOf("</dependencies>")
      if ($idx -ge 0) {
        $insert = "`r`n        <dependency>`r`n            <groupId>org.springdoc</groupId>`r`n            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>`r`n            <version>2.5.0</version>`r`n        </dependency>`r`n    </dependencies>"
        $newContent = $content.Substring(0, $idx) + $insert + $content.Substring($idx + 15)
        Set-Content $file -Value $newContent
        Write-Host "Updated $svc"
      }
    }
  }
}
