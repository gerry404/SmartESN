FROM python:3.13-slim
WORKDIR /app

# dépendances Python (couche cachée tant que requirements ne change pas)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# code de l'application
COPY app ./app

EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
