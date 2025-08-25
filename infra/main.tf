terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.99.1"
    }
    random = {
      source  = "hashicorp/random"
      version = "3.7.2"
    }
  }
}

provider "aws" {
  region = "eu-central-1"
}

resource "aws_ecr_repository" "app_repository" {
  name         = "java-build-automaton"
  force_delete = true

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Project   = "Java Build Automaton"
    ManagedBy = "Terraform"
  }
}

resource "random_password" "db_password" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "aws_security_group" "db_sg" {
  name        = "jba-db-sg"
  description = "Allow traffic to the RDS database"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "JBA DB Security Group"
  }
}


resource "aws_db_subnet_group" "db_subnet_group" {
  name       = "jba-db-subnet-group"
  subnet_ids = [aws_subnet.public_a.id, aws_subnet.public_b.id]

  tags = {
    Name = "JBA DB Subnet Group"
  }
}

resource "aws_db_instance" "app_db" {
  identifier             = "java-build-automaton-db"
  allocated_storage      = 20
  engine                 = "postgres"
  engine_version         = "15"
  instance_class         = "db.t3.micro"
  db_name                = "jbadb"
  username               = "jbauser"
  password               = random_password.db_password.result
  skip_final_snapshot    = true
  publicly_accessible    = false
  vpc_security_group_ids = [aws_security_group.db_sg.id]
  db_subnet_group_name   = aws_db_subnet_group.db_subnet_group.name

  tags = {
    Name = "JBA Database"
  }
}

output "ecr_repository_url" {
  description = "The URL of the ECR repository"
  value       = aws_ecr_repository.app_repository.repository_url
}

output "db_instance_address" {
  description = "The address of the RDS instance"
  value       = aws_db_instance.app_db.address
}
