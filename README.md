# Микросервис ***post-service*** для работы с постами.

## Инструкция по развертыванию микросервиса в кластере Kubernetes

### Требования к программному обеспечению
Для корректной работы и тестирования приложения внутри кластера Kubernetes необходимо наличие следующего установленного
и настроенного ПО
для ОС Windows:
* Minikube;
* Docker;
* Gradle;
* Helm CLI;
* VirtualBox;
* GitLab Runner (на базе исполняемой оболочки PowerShell хост-машины);
* Локальный сервер Sonarqube.

---
### Запуск локального сервера Sonarqube
Для запуска локального сервера Sonarqube внутри запущенного докера необходимо перейти в корневую директорию проекта и
ввести команду:
```bash
docker-compose -f ./docker/docker-compose.yml up -d
```

---


### Инструкция по подготовке кластера Kubernetes к работе
Открыть оболочку командной строки с правами администратора и в ней выполнить следующие действия:
1.  Запустить minikube на Windows командой с использование гипервизора VirtualBox:
```bash
minikube start --vm-driver=virtualbox --no-vtx-check
```

2. Включить входной контроллер NGINX в кластере Kubernetes командой:
```bash
minikube addons enable ingress
```

3. На подготовленной виртуальной машине Minikube'а создать каталоги для баз данных и хранилищ, используя учетные данные
   суперпользователя **root**, а также следующие команды:
```bash
cd ../
mkdir -p post-service/postgresql-storage-default
mkdir -p post-service/postgresql-storage-feature
mkdir -p post-service/postgresql-storage-dev
mkdir -p post-service/postgresql-storage-preprod
mkdir -p post-service/postgresql-storage-prod
mkdir -p post-service/postgresql-storage-test
mkdir -p post-service/minio-storage-default
mkdir -p post-service/minio-storage-feature
mkdir -p post-service/minio-storage-dev
mkdir -p post-service/minio-storage-preprod
mkdir -p post-service/minio-storage-prod
mkdir -p post-service/minio-storage-test
```

4. На локальной хост-машине ввести следующие команды для создания пространства имен для каждой из сред:
```bash
kubectl create namespace feauture
kubectl create namespace dev
kubectl create namespace preprod
kubectl create namespace prod
kubectl create namespace test
```

5. На локальной хост-машине для каждой среды создать **secret**, хранящий настройки подключения к приватному хранилищу
   образов Docker:
```bash
kubectl create secret docker-registry private-docker-registry `
--docker-server=<доменный адрес сервера приватного репозитория> `
--docker-username=<имя пользователя> `
--docker-password=<пароль> `
--docker-email=<адрес почты> `
--namespace=<наименование пространства среды>
```

---


### Деплой приложения в автоматизированном режиме
**ВАЖНО!:** Данный способ работает лишь при работе с версией репозитория проекта, размещенного на сервисе GitLab.<br>

Для автоматизированного развертывания собранного приложения достаточно произвести фиксацию изменений в виде коммитов в одной
из веток: **feature**, **dev** или **preprod**.<br>
При выявлении события фиксации будет производиться тестирование, сборка и поставка обновленной версии микросервиса  с
учетом последних изменений на соответствующий ветке подготовленный стенд.

---

### Деплой приложения в ручном режиме
Если вы хотите выполнить развертывания приложения в ручном режиме в подготовленный кластер Kubernetes, тогда необходимо
руководствоваться действиями, описанными в указанном подразделе.
1. Указать используемому терминалу об использовании внутреннего демона Docker в кластере с помощью команд:
    - Для оболочки **bash**:
       ```bash
       eval $(minikube docker-env)
       ```
    - Для оболочки **PowerShell**:
       ```bash
       minikube docker-env | Invoke-Expression
       ```

2. Перейти в корневую директорию проекта и собрать его при помощи команды:
    ```bash
      ./gradlew build
    ```   

3. Собрать docker-образ микросервиса для интересующего стенда:
    - Для пространства по умолчанию (default)
      ```bash
      docker build -t euchekavelo/backend-post-service:latest-default .
      ```
    - Для стенда **feature**
      ```bash
      docker build -t euchekavelo/backend-post-service:latest-feature .
      ```
    - Для стенда **dev**
      ```bash
      docker build -t euchekavelo/backend-post-service:latest-dev .
      ```
    - Для стенда **preprod**
      ```bash
      docker build -t euchekavelo/backend-post-service:latest-preprod .
      ```    
   - Для стенда **prod**
     ```bash
     docker build -t euchekavelo/backend-post-service:latest-prod .
     ```  
   - Для стенда **test**
     ```bash
     docker build -t euchekavelo/backend-post-service:latest-test .
     ```

4. Внутри корневой папки проекта перейти в директорию **chart** и выполнить ряд команд:
    - Для развертывания chart-файла на **default**-неймспейсе:
         ```bash
         helm upgrade --install backend-post-service ./backend-post-service
         ```
    - Для развертывания chart-файла на **feature**-неймспейсе:
         ```bash
         helm upgrade --install backend-post-service-feature ./backend-post-service -f ./backend-post-service/values-feature.yml
         ```
    - Для развертывания chart-файла на **dev**-неймспейсе:
         ```bash
         helm upgrade --install backend-post-service-dev ./backend-post-service -f ./backend-post-service/values-dev.yml
         ```
    - Для развертывания chart-файла на **preprod**-неймспейсе:
         ```bash
         helm upgrade --install backend-post-service-preprod ./backend-post-service -f ./backend-post-service/values-preprod.yml
         ```
   - Для развертывания chart-файла на **prod**-неймспейсе:
        ```bash
        helm upgrade --install backend-post-service-prod ./backend-post-service -f ./backend-post-service/values-prod.yml
        ```
   - Для развертывания chart-файла на **test**-неймспейсе:
        ```bash
        helm upgrade --install backend-post-service-test ./backend-post-service -f ./backend-post-service/values-test.yml
        ```
---


### Общее для обоих способов деплоя

После развертывания комплекса приложений по любому из способов необходимо у себя 
в системе отредактировать файл **hosts**, указав имена хостов и соответствующие выделенные внешние
ip-адреса ingress'ов каждого контура.
<br>Наименования хостов для каждой из сред можно посмотреть в соответствующих yml-файлах с префиксом ***values***. 
Данные файлы расположены в папке **chart/backend-post-service** корневой директории проета.

---


### Настройка подключения к S3-хранилищу для микросервиса
После успешного развертывания комплекса приложений необходимо зайти в MiniO через веб-интерфейс и создать корзину **posts**, 
сделай ее публичной, а также сгенерировать ключи для пользования API сервера хранилища.
<br>Значения соответствующих ключей необходимо установить в развернутом объекте ConfigMap. После чего потребуется 
перезагрузить Pod микросервиса для применения внесенных изменений.

---