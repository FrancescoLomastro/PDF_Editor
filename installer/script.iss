[Setup]
AppName=JavaPDFEdit
AppVersion=1.0
DefaultDirName={pf}\JavaPDFEdit
OutputDir=Output
OutputBaseFilename=Setup

[Files]
Source: "C:\Users\francesco\Documents\JavaPDFEdit.jar"; DestDir: "{app}"  
Source: "C:\Users\francesco\Desktop\installer\jdk-21.0.2_windows-x64_bin.exe"; DestDir: "{app}"; AfterInstall: InstallJava

[Icons]
Name: "{commonstartmenu}\\JavaPDFEdit"; Filename: "{app}\\JavaPDFEdit.jar"; WorkingDir: "{app}"


[Code]
var 
  ChoicePage: TWizardPage;
  RadioButton1: TRadioButton;
  RadioButton2: TRadioButton;

procedure ChoicePageHandler();
var
  Label1: TLabel;
begin
  ChoicePage := CreateCustomPage(wpWelcome, 'Java JDK?', '');

  RadioButton1 := TRadioButton.Create(ChoicePage);
  RadioButton1.Top := 20;
  RadioButton1.Left := 20;
  RadioButton1.Width := 320;
  RadioButton1.Caption := 'Install also Java JDK 21.0.2.';
  RadioButton1.Parent := ChoicePage.Surface;
  RadioButton1.Checked := True;

  RadioButton2 := TRadioButton.Create(ChoicePage);
  RadioButton2.Top := RadioButton1.Top + 40;
  RadioButton2.Left := 20;
  RadioButton2.Width := 320;
  RadioButton2.Caption := 'Do not install Java JDK.';
  RadioButton2.Parent := ChoicePage.Surface;
  
  Label1 := TLabel.Create(ChoicePage);
  Label1.Parent := ChoicePage.Surface;
  Label1.Caption := '(Check this option if you already have JDK 21.0.2 or higher)';
  Label1.Left := RadioButton2.Left + 5 ; 
  Label1.Top :=  RadioButton2.Top +15;
end;
 
procedure InstallJava;
var
  ResultCode: Integer;
begin
  if RadioButton1.Checked then
    if not Exec(ExpandConstant('{tmp}\jdk-21.0.2_windows-x64_bin.exe'), '', '', SW_SHOWNORMAL, ewWaitUntilTerminated, ResultCode)
    then
      MsgBox('Other installer failed to run!' + #13#10 + SysErrorMessage(ResultCode), mbError, MB_OK);
end;

procedure InitializeWizard;
begin
  ChoicePageHandler();
end;